package eu.ill.visa.web.controllers;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import eu.ill.visa.business.services.ExperimentService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstrumentService;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.ClientConfiguration;
import eu.ill.visa.web.dtos.*;
import io.dropwizard.auth.Auth;
import org.dozer.Mapper;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static eu.ill.visa.core.domain.Role.*;
import static eu.ill.visa.core.domain.enumerations.InstanceMemberRole.OWNER;
import static javax.ws.rs.core.Response.Status.OK;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class AccountController extends AbstractController {

    private final UserService       userService;
    private final InstrumentService instrumentService;
    private final ExperimentService experimentService;
    private final Mapper            mapper;
    private final InstanceService   instanceService;
    private final ClientConfiguration clientConfiguration;

    @Inject
    public AccountController(final UserService userService,
                             final InstrumentService instrumentService,
                             final InstanceService instanceService,
                             final ExperimentService experimentService,
                             final Mapper mapper,
                             final ClientConfiguration clientConfiguration) {
        this.userService = userService;
        this.instrumentService = instrumentService;
        this.instanceService = instanceService;
        this.experimentService = experimentService;
        this.clientConfiguration = clientConfiguration;
        this.mapper = mapper;
    }

    @GET
    public Response get(@Auth final AccountToken accountToken) {
        final User user = accountToken.getUser();
        final UserFullDto userDto = mapper.map(user, UserFullDto.class);
        for (UserRole userRole : user.getActiveUserRoles()) {
            userDto.addActiveUserRole(new RoleDto(userRole.getRole().getName(), userRole.getExpiresAt()));
        }
        for (Role group : user.getGroups()) {
            userDto.addGroup(group.getName());
        }

        return createResponse(userDto, OK);
    }

    @GET
    @Path("/experiments/instruments")
    public Response experimentInstruments(@Auth final AccountToken accountToken) {
        final User user = accountToken.getUser();
        final List<Instrument> instruments = instrumentService.getAllForUser(user);
        return createResponse(instruments, OK);
    }

    @GET
    @Path("/quotas")
    public Response quotas(@Auth final AccountToken accountToken) {
        final User user = accountToken.getUser();
        final QuotaDto dto = new QuotaDto();
        // the user has an invalid employee number... send defaults
        if ("0".equals(user.getId())) {
            dto.setTotalInstances(0L);
            dto.setAvailableInstances(0L);
            dto.setMaxInstances(0);
            return createResponse(dto, OK);
        }
        final Long totalInstances = instanceService.countAllForUserAndRole(user, OWNER);
        dto.setTotalInstances(totalInstances);
        dto.setAvailableInstances(user.getInstanceQuota() - totalInstances);
        dto.setMaxInstances(user.getInstanceQuota());
        return createResponse(dto, OK);
    }

    @GET
    @Path("/experiments/years")
    public Response experimentYears(@Auth final AccountToken accountToken) {
        final User user = accountToken.getUser();
        List<Integer> years = experimentService.getYearsForUser(user, clientConfiguration.getExperimentsConfiguration().getOpenDataIncluded());
        return createResponse(years, OK);
    }

    @GET
    @Path("/experiments")
    public Response experiments(@Auth final AccountToken accountToken,
                                @QueryParam("instrumentId") final Long instrumentId,
                                @QueryParam("startDate") final String startDateString,
                                @QueryParam("endDate") final String endDateString,
                                @QueryParam("proposals") final String proposalsString,
                                @QueryParam("dois") final String doisString,
                                @QueryParam("includeOpenData") @DefaultValue("false") Boolean includeOpenData,
                                @QueryParam("page") @DefaultValue("1") @Min(1) final Integer page,
                                @QueryParam("limit") @DefaultValue("25") @Min(5) @Max(100) final Integer limit,
                                @QueryParam("orderBy") final String orderByValue,
                                @QueryParam("descending") @DefaultValue("false") final boolean descending) {

        try {
            final Instrument instrument = instrumentService.getById(instrumentId);
            final User user = accountToken.getUser();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date startDate = startDateString == null ? null : simpleDateFormat.parse(startDateString);
            Date endDate = endDateString == null ? null : simpleDateFormat.parse(endDateString);
            Set<String> proposalIdentifiers = proposalsString == null ? null : new HashSet<>(Arrays.asList(proposalsString.split(",")));
            Set<String> dois = doisString == null ? null : new HashSet<>(Arrays.asList(doisString.split(",")));

            includeOpenData = includeOpenData && clientConfiguration.getExperimentsConfiguration().getOpenDataIncluded();

            final ExperimentFilter filter = new ExperimentFilter(startDate, endDate, instrument, proposalIdentifiers, dois, includeOpenData);

            final List<ExperimentDto> experiments = new ArrayList<>();
            final Long total = experimentService.getAllCountForUser(user, filter);
            final int offset = (page - 1) * limit;

            final Pagination pagination = new Pagination(limit, offset);
            OrderBy orderBy = null;
            if (orderByValue != null) {
                orderBy = new OrderBy(orderByValue, !descending);
            }

            final ImmutableMap metadata = ImmutableMap.of(
                "count", total,
                "page", page,
                "limit", limit
            );

            for (Experiment experiment : experimentService.getAllForUser(user, filter, pagination, orderBy)) {
                experiments.add(mapper.map(experiment, ExperimentDto.class));
            }

            // Check if proposals was specified whether all the proposals have associated experiments
            List<String> errors = new ArrayList<>();
            if (proposalIdentifiers != null) {
                Set<String> experimentProposalIdentifiers = experiments.stream().map(experiment -> experiment.getProposal().getIdentifier()).collect(Collectors.toSet());
                Set<String> missingProposalIdentifiers = new HashSet<>(proposalIdentifiers);
                missingProposalIdentifiers.removeAll(experimentProposalIdentifiers);
                if (missingProposalIdentifiers.size() > 0) {
                    String error = "Could not obtain experiments for the following proposal(s): " + String.join(", ", missingProposalIdentifiers);
                    errors.add(error);
                }
            }
            if (dois != null) {
                Set<String> experimentDOIs = experiments.stream().map(experiment -> experiment.getDoi() != null ? experiment.getDoi() : experiment.getProposal().getDoi()).collect(Collectors.toSet());
                Set<String> missingDOIs = new HashSet<>(dois);
                missingDOIs.removeAll(experimentDOIs);
                if (missingDOIs.size() > 0) {
                    String error = "Could not obtain experiments for the following doi(s): " + String.join(", ", missingDOIs);
                    errors.add(error);
                }
            }

            return createResponse(experiments, OK, metadata, errors);

        } catch (ParseException e) {
            throw new BadRequestException("Could not convert dates");
        }
    }

    @GET
    @Path("/experiments/_count")
    public Response experiments(@Auth final AccountToken accountToken) {
        final User user = accountToken.getUser();
        final Long total = experimentService.getAllCountForUser(user);
        return createResponse(total, OK);
    }

    @GET
    @Path("/users/_search")
    public Response search(@QueryParam("name") final String name) {
        final List<UserSimpleDto> users = new ArrayList<>();
        if (Objects.isNull(name) || name.length() == 0) {
            return createResponse(users);
        }
        for (final User user : userService.getAllLikeLastName(name, true, new Pagination(250, 0))) {
            users.add(this.mapUserSimpler(user));
        }
        return createResponse(users, OK);
    }

    @GET
    @Path("/users/support")
    public Response search() {
        final List<UserSimpleDto> users = new ArrayList<>();
        for (final User user : userService.getAllSupport()) {
            users.add(this.mapUserSimpler(user));
        }
        return createResponse(users, OK);
    }

    @GET
    @Path("/users/{user}")
    @RolesAllowed({ADMIN_ROLE, INSTRUMENT_CONTROL_ROLE, INSTRUMENT_SCIENTIST_ROLE, IT_SUPPORT_ROLE, STAFF_ROLE})
    public Response get(@PathParam("user") final User user) {
        return createResponse(this.mapUserSimpler(user), OK);
    }

    private UserSimpleDto mapUserSimpler(User user) {
        UserSimpleDto userSimpleDto = mapper.map(user, UserSimpleDto.class);
        for (UserRole userRole : user.getActiveUserRoles()) {
            userSimpleDto.addActiveUserRole(new RoleDto(userRole.getRole().getName(), userRole.getExpiresAt()));
        }
        for (Role group : user.getGroups()) {
            userSimpleDto.addGroup(group.getName());
        }
        return userSimpleDto;
    }
}
