package eu.ill.visa.web.controllers;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import eu.ill.visa.business.services.*;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.DesktopConfiguration;
import eu.ill.visa.web.dtos.*;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.dozer.Mapper;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static eu.ill.visa.core.domain.enumerations.InstanceMemberRole.OWNER;
import static eu.ill.visa.core.domain.enumerations.InstanceMemberRole.SUPPORT;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.OK;

@Path("/account/instances")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Api(tags = {"Account"}, description = "Account instances operations")
@PermitAll
public class AccountInstanceController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(AccountInstanceController.class);
    private final InstanceService instanceService;
    private final InstanceMemberService instanceMemberService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceCommandService instanceCommandService;
    private final InstanceExpirationService instanceExpirationService;
    private final InstanceAuthenticationTokenService instanceAuthenticationTokenService;
    private final PlanService planService;
    private final UserService userService;
    private final ExperimentService experimentService;
    private final NotificationService notificationService;
    private final DesktopConfiguration desktopConfiguration;
    private final Mapper mapper;

    @Inject
    public AccountInstanceController(final UserService userService,
                                     final InstanceService instanceService,
                                     final InstanceMemberService instanceMemberService,
                                     final InstanceSessionService instanceSessionService,
                                     final InstanceCommandService instanceCommandService,
                                     final InstanceExpirationService instanceExpirationService,
                                     final InstanceAuthenticationTokenService instanceAuthenticationTokenService,
                                     final PlanService planService,
                                     final ExperimentService experimentService,
                                     final NotificationService notificationService,
                                     final DesktopConfiguration desktopConfiguration,
                                     final Mapper mapper) {
        this.userService = userService;
        this.instanceService = instanceService;
        this.instanceMemberService = instanceMemberService;
        this.instanceSessionService = instanceSessionService;
        this.instanceCommandService = instanceCommandService;
        this.instanceExpirationService = instanceExpirationService;
        this.instanceAuthenticationTokenService = instanceAuthenticationTokenService;
        this.planService = planService;
        this.experimentService = experimentService;
        this.notificationService = notificationService;
        this.desktopConfiguration = desktopConfiguration;
        this.mapper = mapper;
    }

    @GET
    public Response getAll(@Auth final AccountToken accountToken,
                           @QueryParam("roles") String roles,
                           @QueryParam("experiments") String experiments) {
        final User user = accountToken.getUser();
        final List<InstanceDto> instances = instanceService.getAllForUser(user).stream()
            .filter(instance -> {
                final InstanceMember member = instanceMemberService.getByInstanceAndUser(instance, user);
                if (member == null) {
                    return false;
                }
                if (roles == null) {
                    return true;
                }
                return asList(
                    requireNonNullElse(roles, "").split(",")
                ).contains(member.getRole().name());
            })
            .filter(instance -> {
                if (experiments == null) {
                    return true;
                }
                return instance.getExperiments().stream().anyMatch(experiment -> {
                    return asList(
                        requireNonNullElse(experiments, "").split(",")
                    ).contains(experiment.getId());
                });
            })
            .map(instance -> this.mapInstance(instance, user))
            .collect(toList());
        return createResponse(instances);
    }

    @GET
    @Path("/_count")
    public Response getCount(@Auth final AccountToken accountToken) {
        final User user = accountToken.getUser();
        final Long instances = instanceService.countAllForUser(user);
        return createResponse(instances);
    }

    @GET
    @Path("/experiments")
    public Response getAllExperiments(@Auth final AccountToken accountToken) {
        final User user = accountToken.getUser();
        final List<Experiment> experiments = new ArrayList<>();
        for (final Instance instance : instanceService.getAllForUser(user)) {
            for (final Experiment experiment : instance.getExperiments()) {
                if (!experiments.contains(experiment)) {
                    experiments.add(experiment);
                }
            }
        }
        final List<ExperimentDto> data = experiments
            .stream()
            .map(experiment -> mapper.map(experiment, ExperimentDto.class))
            .collect(toList());

        return createResponse(data);
    }


    @GET
    @Path("/support")
    public Response getAllForSupport(@Auth final AccountToken accountToken,
                                     @QueryParam("id") final Long instanceId,
                                     @QueryParam("name") final String instanceName,
                                     @QueryParam("owner") final String owner,
                                     @QueryParam("instrumentId") final Long instrumentId,
                                     @QueryParam("page") @DefaultValue("1") @Min(1) final Integer page,
                                     @QueryParam("limit") @DefaultValue("25") @Min(5) @Max(100) final Integer limit,
                                     @QueryParam("orderBy") @DefaultValue("id") final String orderByValue,
                                     @QueryParam("descending") @DefaultValue("false") final boolean descending) {
        final User user = accountToken.getUser();

        final int offset = (page - 1) * limit;

        final InstanceFilter filter = new InstanceFilter(instanceId, owner, instanceName, instrumentId);
        final Pagination pagination = new Pagination(limit, offset);
        final OrderBy orderBy = new OrderBy(orderByValue, !descending);
        final Long total = instanceService.countAllForSupportUser(user, filter);


        final ImmutableMap metadata = ImmutableMap.of(
            "count", total,
            "page", page,
            "limit", limit
        );

        final List<InstanceDto> instances = instanceService.getAllForSupportUser(user, filter, orderBy, pagination).stream()
            .map(instance -> this.mapInstance(instance, user))
            .collect(toList());
        return createResponse(instances, OK, metadata);
    }

    @GET
    @Path("/{instance}")
    public Response get(@Auth final AccountToken accountToken, @PathParam("instance") Instance instance) {
        final User user = accountToken.getUser();

        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            InstanceDto instanceDto = this.mapInstance(instance, user);

            return createResponse(instanceDto);
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/{instance}/state")
    public Response getState(@Auth final AccountToken accountToken, @PathParam("instance") Instance instance) {
        final User user = accountToken.getUser();

        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            InstanceStateDto instanceStateDto = mapper.map(instance, InstanceStateDto.class);
            instanceStateDto.setExpirationDate(instanceExpirationService.getExpirationDate(instance));
            return createResponse(instanceStateDto);
        }

        throw new NotFoundException();
    }

    @POST
    @Path("/{instance}/actions/reboot")
    public Response rebootAction(@Auth final AccountToken accountToken, @PathParam("instance") Instance instance) {
        return this.performAction(instance, accountToken.getUser(), InstanceCommandType.REBOOT);
    }

    @POST
    @Path("/{instance}/actions/shutdown")
    public Response shutdownAction(@Auth final AccountToken accountToken, @PathParam("instance") Instance instance) {
        return this.performAction(instance, accountToken.getUser(), InstanceCommandType.SHUTDOWN);
    }

    @POST
    @Path("/{instance}/actions/start")
    public Response startAction(@Auth final AccountToken accountToken, @PathParam("instance") final Instance instance) {
        return this.performAction(instance, accountToken.getUser(), InstanceCommandType.START);
    }

    @POST
    public Response create(@Auth final AccountToken accountToken, @NotNull @Valid final InstanceCreatorDto dto) {
        final User user = accountToken.getUser();
        if (user.getInstanceQuota() != -1) {
            if (instanceService.countAllForUserAndRole(user, OWNER) >= user.getInstanceQuota()) {
                logger.info("User {} ({}) has exceeded their quota of {} instances", user.getFullName(), user.getId(), user.getInstanceQuota());
                throw new BadRequestException("Instance quota exceeded");
            }
        }

        if (this.desktopConfiguration.getKeyboardLayoutForLayout(dto.getKeyboardLayout()) == null) {
            throw new BadRequestException("Invalid keyboard layout provided");
        }

        final String connectedUsername = accountToken.getName();
        final Plan plan = planService.getById(dto.getPlanId());
        checkNotNull(plan, "Invalid plan");


        // Verify user has access to experiments
        List<Experiment> experiments = new ArrayList<>();
        dto.getExperiments().forEach(experimentId -> {
            Experiment experiment = experimentService.getByIdAndUser(experimentId, user);
            if (experiment != null) {
                experiments.add(experiment);
            }
        });

        if (experiments.size() != dto.getExperiments().size()) {
            throw new NotAuthorizedException("Not authorized to associate instance to requested experiments");

        } else if (user.hasRole(Role.ADMIN_ROLE)) {
            List<Plan> plansForAdmin = this.planService.getAllForAdmin();
            if (plansForAdmin.stream().noneMatch(aPlan -> aPlan.getId().equals(plan.getId()))) {
                throw new NotAuthorizedException("Not authorized to create an instance with the selected Plan for the requested experiments");
            }

        } else if (experiments.size() == 0) {
            // Validate no experiments valid for user (only admin and scientific support)
            if (!user.hasAnyRole(List.of(Role.ADMIN_ROLE, Role.STAFF_ROLE))) {
                throw new NotAuthorizedException("Not authorized to create an instance without any associated experiments");
            }
            List<Plan> plansForInstruments = this.planService.getAllForUserAndAllInstruments(user);
            if (plansForInstruments.stream().noneMatch(aPlan -> aPlan.getId().equals(plan.getId()))) {
                throw new NotAuthorizedException("Not authorized to create an instance with the selected Plan without any selected experiments");
            }

        } else {
            // Validate plan for experiments
            List<Plan> plansForInstruments = this.planService.getAllForUserAndExperiments(user, experiments);
            if (plansForInstruments.stream().noneMatch(aPlan -> aPlan.getId().equals(plan.getId()))) {
                throw new NotAuthorizedException("Not authorized to create an instance with the selected Plan for the requested experiments");
            }
        }

        List<InstanceAttribute> instanceAttributes = new ArrayList<>();
        accountToken.getAccountParameters().forEach((s, s2) -> instanceAttributes.add(new InstanceAttribute(s, s2)));
        Instance.Builder instanceBuilder = Instance.builder()
            .plan(plan)
            .name(dto.getName())
            .comments(dto.getComments())
            .screenWidth(dto.getScreenWidth())
            .screenHeight(dto.getScreenHeight())
            .keyboardLayout(dto.getKeyboardLayout())
            .username(connectedUsername)
            .member(user, OWNER)
            .experiments(experiments)
            .attributes(instanceAttributes);

        Instance instance = instanceService.create(instanceBuilder);

        // Create the command and let the scheduler manage the execution
        instanceCommandService.create(user, instance, InstanceCommandType.CREATE);

        InstanceDto instanceDto = this.mapInstance(instance, user);

        return createResponse(instanceDto);
    }

    @PUT
    @Path("/{instance}")
    public Response update(@Auth final AccountToken accountToken,
                           @PathParam("instance") Instance instance,
                           @Valid @NotNull final InstanceUpdatorDto instanceUpdatorDto) {
        final User user = accountToken.getUser();
        if (this.instanceService.isAuthorisedForInstance(user, instance, OWNER)) {
            if (this.desktopConfiguration.getKeyboardLayoutForLayout(instanceUpdatorDto.getKeyboardLayout()) == null) {
                throw new BadRequestException("Invalid keyboard layout provided");
            }

            instance.setName(instanceUpdatorDto.getName());
            instance.setComments(instanceUpdatorDto.getComments());
            instance.setScreenWidth(instanceUpdatorDto.getScreenWidth());
            instance.setScreenHeight(instanceUpdatorDto.getScreenHeight());
            instance.setKeyboardLayout(instanceUpdatorDto.getKeyboardLayout());
            instanceService.save(instance);
            return createResponse(mapper.map(instance, InstanceDto.class));
        }
        throw new NotFoundException();
    }


    @DELETE
    @Path("/{instance}")
    public Response delete(@Auth final AccountToken accountToken, @PathParam("instance") final Instance instance) {
        if (instance.getComputeId() == null || instance.hasAnyState(List.of(InstanceState.STOPPED, InstanceState.ERROR, InstanceState.UNKNOWN, InstanceState.UNAVAILABLE))) {
            return this.performAction(instance, accountToken.getUser(), InstanceCommandType.DELETE);

        } else {
            instance.setDeleteRequested(true);
            this.instanceService.save(instance);
            if (instance.getState().equals(InstanceState.STOPPING)) {
                return createResponse(mapInstance(instance, accountToken.getUser()));

            } else {
                return this.performAction(instance, accountToken.getUser(), InstanceCommandType.SHUTDOWN);
            }
        }
    }

    @GET
    @Path("/{instance}/experiments/team")
    public Response getTeam(@Auth final AccountToken accountToken, @PathParam("instance") Instance instance) {
        final User user = accountToken.getUser();
        final List<User> team = userService.getExperimentalTeamForInstance(instance);
        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            return createResponse(team.stream().map(this::mapUserSimpler).collect(toList()));
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/{instance}/members")
    public Response allMembers(@Auth final AccountToken accountToken, @PathParam("instance") Instance instance) {
        final User user = accountToken.getUser();

        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            return createResponse(instance.getMembers().stream().map(member -> mapper
                .map(member, InstanceMemberDto.class))
                .sorted()
                .collect(toList()));
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/{instance}/sessions/active/members")
    public Response allActiveSessionMembers(@Auth final AccountToken accountToken, @PathParam("instance") Instance instance) {
        final User user = accountToken.getUser();

        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            List<InstanceSessionMember> sessionMembers = this.instanceSessionService.getAllSessionMembers(instance);

            return createResponse(sessionMembers.stream().map(sessionMember -> mapper
                .map(sessionMember, InstanceSessionMemberDto.class))
                .collect(toList()));
        }

        throw new NotFoundException();
    }


    @POST
    @Path("/{instance}/auth/token")
    public Response createInstanceAuthenticationTicket(@Auth final AccountToken accountToken, @PathParam("instance") Instance instance) {
        final User user = accountToken.getUser();
        final InstanceMember member = instance.getMember(user);

        if (!this.instanceService.isAuthorisedForInstance(user, instance)) {
            throw new NotAuthorizedException("You do not have permission");
        }

        // Check that we have a correct username for the instance
        if ((member == null || !member.getRole().equals(OWNER)) && instance.getUsername() == null) {
            throw new NotAuthorizedException("You do not have permission");
        }

        final InstanceAuthenticationToken token = instanceAuthenticationTokenService.create(user, instance);

        return createResponse(new InstanceAuthenticationTokenDto(token), CREATED);
    }

    @POST
    @Path("/{instance}/members")
    public Response addMember(@Auth final AccountToken accountToken,
                              @PathParam("instance") final Instance instance,
                              @Valid @NotNull final InstanceMemberCreatorDto instanceMemberCreatorDto) {
        final User user = accountToken.getUser();
        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            User memberUser = userService.getById(instanceMemberCreatorDto.getUserId());
            if (memberUser != null) {
                InstanceMember instanceMember = InstanceMember.newBuilder()
                    .user(memberUser)
                    .role(instanceMemberCreatorDto.getRole())
                    .build();

                instance.addMember(instanceMember);

                this.instanceService.save(instance);

                notificationService.sendInstanceMemberAddedNotification(instance, instanceMember);

                return createResponse();

            } else {
                throw new BadRequestException("User not found");
            }

        } else if (instance.isMember(user)) {
            throw new NotAuthorizedException("Not authorized to perform this action");
        }

        throw new NotFoundException();
    }

    @PUT
    @Path("/{instance}/members/{member}")
    public Response updateMember(@Auth final AccountToken accountToken,
                                 @PathParam("instance") Instance instance,
                                 @PathParam("member") InstanceMember instanceMember,
                                 @Valid @NotNull final InstanceMemberUpdatorDto memberUpdateDto) {
        final User user = accountToken.getUser();
        if (this.instanceService.isAuthorisedForInstance(user, instance, OWNER)) {
            if (instance.isMember(instanceMember)) {
                if (memberUpdateDto.isRole(OWNER)) {
                    throw new BadRequestException("Cannot make a member an owner. There can only be one owner.");
                }
                instanceMember.setRole(memberUpdateDto.getRole());
                instanceMemberService.save(instanceMember);
                return createResponse(mapper.map(instanceMember, InstanceMemberDto.class));
            }
        }
        throw new NotFoundException();
    }

    @DELETE
    @Path("/{instance}/members/{member}")
    public Response removeMember(@Auth final AccountToken accountToken,
                                 @PathParam("instance") Instance instance,
                                 @PathParam("member") InstanceMember instanceMember) {
        final User user = accountToken.getUser();
        if (this.instanceService.isAuthorisedForInstance(user, instance, OWNER)) {
            if (instance.isMember(instanceMember) && !instanceMember.isRole(OWNER)) {
                instance.removeMember(instanceMember);

                instanceService.save(instance);
                return createResponse();
            }
        } else if (instance.isMember(user)) {
            throw new NotAuthorizedException("Not authorized to perform this action");
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/{instance}/history")
    public Response removeMember(@Auth final AccountToken accountToken,
                                 @PathParam("instance") Instance instance) {
        final User user = accountToken.getUser();
        if (this.instanceService.isAuthorisedForInstance(user, instance, OWNER)) {
            List<InstanceCommand> commands = instanceCommandService.getAllForInstance(instance);

            List<InstanceCommandDto> history = commands.stream().map(instanceCommand -> mapper
                .map(instanceCommand, InstanceCommandDto.class))
                .collect(toList());

            return createResponse(history);

        } else if (instance.isMember(user)) {
            throw new NotAuthorizedException("Not authorized to perform this action");
        }

        throw new NotFoundException();
    }

    private Response performAction(final Instance instance, final User user, final InstanceCommandType instanceCommandType) {
        if (this.instanceService.isAuthorisedForInstance(user, instance, OWNER)) {
            if (instanceCommandType.equals(InstanceCommandType.START)) {
                instance.setState(InstanceState.STARTING);

            } else if (instanceCommandType.equals(InstanceCommandType.REBOOT)) {
                instance.setState(InstanceState.REBOOTING);

            } else if (instanceCommandType.equals(InstanceCommandType.SHUTDOWN)) {
                instance.setState(InstanceState.STOPPING);

            } else if (instanceCommandType.equals(InstanceCommandType.DELETE)) {
                instance.setState(InstanceState.DELETING);
            }
            instanceService.save(instance);

            // Create the command and let the scheduler manage the execution
            instanceCommandService.create(user, instance, instanceCommandType);

            return createResponse(mapInstance(instance, user));

        } else if (instance.isMember(user)) {
            throw new NotAuthorizedException("Not authorized to perform this action");
        }

        throw new NotFoundException();
    }

    private InstanceDto mapInstance(Instance instance, User user) {
        InstanceDto instanceDto = mapper.map(instance, InstanceDto.class);
        InstanceMember instanceMember = instanceMemberService.getByInstanceAndUser(instance, user);
        if (instanceMember != null) {
            instanceDto.setMembership(mapper.map(instanceMember, InstanceMemberDto.class));

        } else if (user.hasRole(Role.ADMIN_ROLE)) {
            instanceDto.setMembership(new InstanceMemberDto(this.mapUserSimpler(user), SUPPORT));

        } else if (user.hasAnyRole(List.of(Role.IT_SUPPORT_ROLE, Role.INSTRUMENT_CONTROL_ROLE, Role.INSTRUMENT_SCIENTIST_ROLE))) {
            instanceDto.setMembership(new InstanceMemberDto(this.mapUserSimpler(user), SUPPORT));
        }
        instanceDto.setExpirationDate(instanceExpirationService.getExpirationDate(instance));
        instanceDto.setCanConnectWhileOwnerAway(instanceSessionService.canConnectWhileOwnerAway(instance, user));

        return instanceDto;
    }

    private UserSimpleDto mapUserSimpler(User user) {
        UserSimpleDto userSimpleDto = mapper.map(user, UserSimpleDto.class);
        for (Role role : user.getRoles()) {
            userSimpleDto.addRole(role.getName());
        }
        return userSimpleDto;
    }


    @POST
    @Path("/{instance}/thumbnail")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void createThumbnail(@Auth final AccountToken accountToken,
                                @PathParam("instance") final Instance instance,
                                @NotNull @FormDataParam("file") final InputStream is) {
        try {
            final User user = accountToken.getUser();
            if (this.instanceService.isAuthorisedForInstance(user, instance, OWNER)) {
                final byte[] data = is.readAllBytes();
                final ImageFormat mimeType = Imaging.guessFormat(data);
                if (mimeType == ImageFormats.JPEG) {
                    instanceService.createOrUpdateThumbnail(instance, data);
                }
            }
        } catch (IOException | ImageReadException exception) {
            logger.error("Error creating thumbnail for instance: {}", instance.getId(), exception);
        }
    }

    @GET
    @Path("/{instance}/thumbnail")
    @Produces("image/jpeg")
    public Response getThumbnail(@Auth final AccountToken accountToken,
                                 @PathParam("instance") final Instance instance) {
        final User user = accountToken.getUser();
        if (this.instanceService.isAuthorisedForInstance(user, instance, OWNER)) {
            final InstanceThumbnail thumbnail = instanceService.getThumbnailForInstance(instance);
            if (thumbnail != null) {
                final ResponseBuilder response = Response.ok(thumbnail.getData());
                return response.build();
            }
        }
        // return fallback image...
        final InputStream data = getClass().getResourceAsStream("/images/thumbnail.jpg");
        final ResponseBuilder response = Response.ok(data);
        return response.build();
    }
}
