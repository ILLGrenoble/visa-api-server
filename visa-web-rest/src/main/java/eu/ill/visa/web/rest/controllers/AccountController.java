package eu.ill.visa.web.rest.controllers;

import com.github.dozermapper.core.Mapper;
import eu.ill.visa.business.services.ExperimentService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstrumentService;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.domain.ExperimentFilter;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.web.rest.ClientConfiguration;
import eu.ill.visa.web.rest.dtos.*;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static eu.ill.visa.core.entity.Role.*;
import static eu.ill.visa.core.entity.enumerations.InstanceMemberRole.OWNER;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class AccountController extends AbstractController {

    private final UserService userService;
    private final InstrumentService instrumentService;
    private final ExperimentService experimentService;
    private final Mapper mapper;
    private final InstanceService instanceService;
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
    public MetaResponse<UserFullDto> get(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);
        final UserFullDto userDto = mapper.map(user, UserFullDto.class);
        for (UserRole userRole : user.getActiveUserRoles()) {
            userDto.addActiveUserRole(new RoleDto(userRole.getRole().getName(), userRole.getExpiresAt()));
        }
        for (Role group : user.getGroups()) {
            userDto.addGroup(group.getName());
        }

        return createResponse(userDto);
    }

    @GET
    @Path("/experiments/instruments")
    public MetaResponse<List<InstrumentDto>> experimentInstruments(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);
        final List<Instrument> instruments = instrumentService.getAllForUser(user);
        return createResponse(instruments.stream()
            .map(instrument -> mapper.map(instrument, InstrumentDto.class)).toList());
    }

    @GET
    @Path("/quotas")
    public MetaResponse<QuotaDto> quotas(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);
        final QuotaDto dto = new QuotaDto();
        // the user has an invalid employee number... send defaults
        if ("0".equals(user.getId())) {
            dto.setTotalInstances(0L);
            dto.setAvailableInstances(0L);
            dto.setMaxInstances(0);
            return createResponse(dto);
        }
        final Long totalInstances = instanceService.countAllForUserAndRole(user, OWNER);
        dto.setTotalInstances(totalInstances);
        dto.setAvailableInstances(user.getInstanceQuota() - totalInstances);
        dto.setMaxInstances(user.getInstanceQuota());
        return createResponse(dto);
    }

    @GET
    @Path("/experiments/years")
    public MetaResponse<List<Integer>> experimentYears(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);
        List<Integer> years = experimentService.getYearsForUser(user, clientConfiguration.experimentsConfiguration().openDataIncluded());
        return createResponse(years);
    }

    @GET
    @Path("/experiments")
    public MetaResponse<List<ExperimentDto>> experiments(@Context SecurityContext securityContext,
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
            final Instrument instrument = instrumentId == null ? null : instrumentService.getById(instrumentId);
            final User user = this.getUserPrincipal(securityContext);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date startDate = startDateString == null ? null : simpleDateFormat.parse(startDateString);
            Date endDate = endDateString == null ? null : simpleDateFormat.parse(endDateString);
            Set<String> proposalIdentifiers = proposalsString == null ? null : new HashSet<>(Arrays.asList(proposalsString.split(",")));
            Set<String> dois = doisString == null ? null : new HashSet<>(Arrays.asList(doisString.split(",")));

            includeOpenData = includeOpenData && clientConfiguration.experimentsConfiguration().openDataIncluded();

            final ExperimentFilter filter = new ExperimentFilter(startDate, endDate, instrument, proposalIdentifiers, dois, includeOpenData);

            final List<ExperimentDto> experiments = new ArrayList<>();
            final Long total = experimentService.getAllCountForUser(user, filter);
            final int offset = (page - 1) * limit;

            final Pagination pagination = new Pagination(limit, offset);
            OrderBy orderBy = null;
            if (orderByValue != null) {
                orderBy = new OrderBy(orderByValue, !descending);
            }

            final MetaResponse.MetaData metadata = new MetaResponse.MetaData()
                .count(total)
                .page(page)
                .limit(limit);

            for (Experiment experiment : experimentService.getAllForUser(user, filter, pagination, orderBy)) {
                experiments.add(mapper.map(experiment, ExperimentDto.class));
            }

            // Check if proposals was specified whether all the proposals have associated experiments
            List<String> errors = new ArrayList<>();
            if (proposalIdentifiers != null) {
                Set<String> experimentProposalIdentifiers = experiments.stream().map(experiment -> experiment.getProposal().getIdentifier()).collect(Collectors.toSet());
                Set<String> missingProposalIdentifiers = new HashSet<>(proposalIdentifiers);
                missingProposalIdentifiers.removeAll(experimentProposalIdentifiers);
                if (!missingProposalIdentifiers.isEmpty()) {
                    String error = "Could not obtain experiments for the following proposal(s): " + String.join(", ", missingProposalIdentifiers);
                    errors.add(error);
                }
            }
            if (dois != null) {
                Set<String> experimentDOIs = experiments.stream().map(experiment -> experiment.getDoi() != null ? experiment.getDoi() : experiment.getProposal().getDoi()).collect(Collectors.toSet());
                Set<String> missingDOIs = new HashSet<>(dois);
                missingDOIs.removeAll(experimentDOIs);
                if (!missingDOIs.isEmpty()) {
                    String error = "Could not obtain experiments for the following doi(s): " + String.join(", ", missingDOIs);
                    errors.add(error);
                }
            }

            return createResponse(experiments, metadata, errors);

        } catch (ParseException e) {
            throw new BadRequestException("Could not convert dates");
        }
    }

    @GET
    @Path("/experiments/_count")
    public MetaResponse<Long> experiments(@Context SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);
        final Long total = experimentService.getAllCountForUser(user);
        return createResponse(total);
    }

    @GET
    @Path("/users/_search")
    public MetaResponse<List<UserSimpleDto>> search(@QueryParam("name") final String name) {
        final List<UserSimpleDto> users = new ArrayList<>();
        if (Objects.isNull(name) || name.isEmpty()) {
            return createResponse(users);
        }
        for (final User user : userService.getAllLikeLastName(name, true, new Pagination(250, 0))) {
            users.add(this.mapUserSimple(user));
        }
        return createResponse(users);
    }

    @GET
    @Path("/users/support")
    public MetaResponse<List<UserSimpleDto>> search() {
        final List<UserSimpleDto> users = new ArrayList<>();
        for (final User user : userService.getAllSupport()) {
            users.add(this.mapUserSimpleWithRoles(user));
        }
        return createResponse(users);
    }

    @GET
    @Path("/users/{user}")
    @RolesAllowed({ADMIN_ROLE, INSTRUMENT_CONTROL_ROLE, INSTRUMENT_SCIENTIST_ROLE, IT_SUPPORT_ROLE, STAFF_ROLE})
    public MetaResponse<UserSimpleDto> get(@PathParam("user") final User user) {
        return createResponse(this.mapUserSimpleWithRoles(user));
    }

    private UserSimpleDto mapUserSimple(User user) {
        return mapper.map(user, UserSimpleDto.class);
    }

    private UserSimpleDto mapUserSimpleWithRoles(User user) {
        UserSimpleDto userSimpleDto = this.mapUserSimple(user);
        for (UserRole userRole : user.getActiveUserRoles()) {
            userSimpleDto.addActiveUserRole(new RoleDto(userRole.getRole().getName(), userRole.getExpiresAt()));
        }
        for (Role group : user.getGroups()) {
            userSimpleDto.addGroup(group.getName());
        }
        return userSimpleDto;
    }
}
