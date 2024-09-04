package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.notification.EmailManager;
import eu.ill.visa.business.services.*;
import eu.ill.visa.core.domain.InstanceFilter;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.rest.ClientConfiguration;
import eu.ill.visa.web.rest.DesktopConfigurationImpl;
import eu.ill.visa.web.rest.dtos.*;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.ill.visa.core.entity.enumerations.InstanceMemberRole.OWNER;
import static eu.ill.visa.core.entity.enumerations.InstanceMemberRole.SUPPORT;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jboss.resteasy.reactive.RestResponse.Status.CREATED;

@Path("/account/instances")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Authenticated
public class AccountInstanceController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(AccountInstanceController.class);
    private final InstanceService instanceService;
    private final InstanceMemberService instanceMemberService;
    private final InstanceSessionService instanceSessionService;
    private final InstanceCommandService instanceCommandService;
    private final InstanceExtensionRequestService instanceExtensionRequestService;
    private final InstanceAuthenticationTokenService instanceAuthenticationTokenService;
    private final PlanService planService;
    private final UserService userService;
    private final ExperimentService experimentService;
    private final EmailManager emailManager;
    private final DesktopConfigurationImpl desktopConfiguration;
    private final ClientConfiguration clientConfiguration;

    @Inject
    public AccountInstanceController(final UserService userService,
                                     final InstanceService instanceService,
                                     final InstanceMemberService instanceMemberService,
                                     final InstanceSessionService instanceSessionService,
                                     final InstanceCommandService instanceCommandService,
                                     final InstanceExtensionRequestService instanceExtensionRequestService,
                                     final InstanceAuthenticationTokenService instanceAuthenticationTokenService,
                                     final PlanService planService,
                                     final ExperimentService experimentService,
                                     final EmailManager emailManager,
                                     final DesktopConfigurationImpl desktopConfiguration,
                                     final ClientConfiguration clientConfiguration) {
        this.userService = userService;
        this.instanceService = instanceService;
        this.instanceMemberService = instanceMemberService;
        this.instanceSessionService = instanceSessionService;
        this.instanceCommandService = instanceCommandService;
        this.instanceExtensionRequestService = instanceExtensionRequestService;
        this.instanceAuthenticationTokenService = instanceAuthenticationTokenService;
        this.planService = planService;
        this.experimentService = experimentService;
        this.emailManager = emailManager;
        this.desktopConfiguration = desktopConfiguration;
        this.clientConfiguration = clientConfiguration;
    }

    @GET
    public MetaResponse<List<InstanceDto>> getAll(@Context final SecurityContext securityContext,
                                                  @QueryParam("roles") String roles,
                                                  @QueryParam("experiments") String experiments) {
        final User user = this.getUserPrincipal(securityContext);
        final List<InstanceDto> instances = instanceService.getAllForUser(user).stream()
            .filter(instance -> {
                final InstanceMember member = instanceMemberService.getByInstanceAndUser(instance, user);
                if (member == null) {
                    return false;
                }
                if (roles == null) {
                    return true;
                }
                return asList(roles.split(",")).contains(member.getRole().name());
            })
            .filter(instance -> {
                if (experiments == null) {
                    return true;
                }
                return instance.getExperiments().stream().anyMatch(experiment -> {
                    return asList(experiments.split(",")).contains(experiment.getId());
                });
            })
            .map(instance -> this.mapInstance(instance, user))
            .collect(toList());
        return createResponse(instances);
    }

    @GET
    @Path("/_count")
    public MetaResponse<Long> getCount(@Context final SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);
        final Long instances = instanceService.countAllForUser(user);
        return createResponse(instances);
    }

    @GET
    @Path("/experiments")
    public MetaResponse<List<ExperimentDto>> getAllExperiments(@Context final SecurityContext securityContext) {
        final User user = this.getUserPrincipal(securityContext);
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
            .map(ExperimentDto::new)
            .collect(toList());

        return createResponse(data);
    }


    @GET
    @Path("/support")
    public MetaResponse<List<InstanceDto>> getAllForSupport(@Context final SecurityContext securityContext,
                                     @QueryParam("id") final Long instanceId,
                                     @QueryParam("name") final String instanceName,
                                     @QueryParam("owner") final String owner,
                                     @QueryParam("instrumentId") final Long instrumentId,
                                     @QueryParam("page") @DefaultValue("1") @Min(1) final Integer page,
                                     @QueryParam("limit") @DefaultValue("25") @Min(5) @Max(100) final Integer limit,
                                     @QueryParam("orderBy") @DefaultValue("id") final String orderByValue,
                                     @QueryParam("descending") @DefaultValue("false") final boolean descending) {
        final User user = this.getUserPrincipal(securityContext);

        final int offset = (page - 1) * limit;

        final InstanceFilter filter = new InstanceFilter(instanceId, owner, instanceName, instrumentId);
        final Pagination pagination = new Pagination(limit, offset);
        final OrderBy orderBy = new OrderBy(orderByValue, !descending);
        final Long total = instanceService.countAllForSupportUser(user, filter);

        final MetaResponse.MetaData metadata = new MetaResponse.MetaData()
            .count(total)
            .page(page)
            .limit(limit);

        final List<InstanceDto> instances = instanceService.getAllForSupportUser(user, filter, orderBy, pagination).stream()
            .map(instance -> this.mapInstance(instance, user))
            .collect(toList());
        return createResponse(instances, metadata);
    }

    @GET
    @Path("/{instance}")
    public MetaResponse<InstanceDto> get(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance) {
        final User user = this.getUserPrincipal(securityContext);

        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            InstanceDto instanceDto = this.mapInstance(instance, user);

            return createResponse(instanceDto);
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/{instance}/state")
    public MetaResponse<InstanceStateDto> getState(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance) {
        final User user = this.getUserPrincipal(securityContext);

        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            InstanceStateDto instanceStateDto = new InstanceStateDto(instance);
            return createResponse(instanceStateDto);
        }

        throw new NotFoundException();
    }

    @POST
    @Path("/{instance}/actions/reboot")
    public MetaResponse<InstanceDto> rebootAction(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance) {
        return this.performAction(instance, this.getUserPrincipal(securityContext), InstanceCommandType.REBOOT);
    }

    @POST
    @Path("/{instance}/actions/shutdown")
    public MetaResponse<InstanceDto> shutdownAction(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance) {
        return this.performAction(instance, this.getUserPrincipal(securityContext), InstanceCommandType.SHUTDOWN);
    }

    @POST
    @Path("/{instance}/actions/start")
    public MetaResponse<InstanceDto> startAction(@Context final SecurityContext securityContext, @PathParam("instance") final Instance instance) {
        return this.performAction(instance, this.getUserPrincipal(securityContext), InstanceCommandType.START);
    }

    @POST
    public MetaResponse<InstanceDto> create(@Context final SecurityContext securityContext, @NotNull @Valid final InstanceCreatorDto dto) {
        final User user = this.getUserPrincipal(securityContext);
        final AccountToken accountToken = this.getAccountToken(securityContext);
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
            Experiment experiment = experimentService.getByIdAndUser(experimentId, user, clientConfiguration.experimentsConfiguration().openDataIncluded());
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

        } else if (experiments.isEmpty()) {
            // Validate no experiments valid for user (only admin and scientific support)
            if (!user.hasAnyRole(List.of(Role.ADMIN_ROLE, Role.STAFF_ROLE, Role.GUEST_ROLE))) {
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
    public MetaResponse<InstanceDto> update(@Context final SecurityContext securityContext,
                                            @PathParam("instance") Instance instance,
                                            @Valid @NotNull final InstanceUpdatorDto instanceUpdatorDto) {
        final User user = this.getUserPrincipal(securityContext);
        if (this.instanceService.isOwnerOrAdmin(user, instance)) {
            if (this.desktopConfiguration.getKeyboardLayoutForLayout(instanceUpdatorDto.getKeyboardLayout()) == null) {
                throw new BadRequestException("Invalid keyboard layout provided");
            }

            instance.setName(instanceUpdatorDto.getName());
            instance.setComments(instanceUpdatorDto.getComments());
            instance.setScreenWidth(instanceUpdatorDto.getScreenWidth());
            instance.setScreenHeight(instanceUpdatorDto.getScreenHeight());
            instance.setKeyboardLayout(instanceUpdatorDto.getKeyboardLayout());
            if (instanceUpdatorDto.getUnrestrictedAccess() && !instance.canAccessWhenOwnerAway()) {
                instance.setUnrestrictedMemberAccess(new Date());
            } else if (!instanceUpdatorDto.getUnrestrictedAccess()) {
                instance.setUnrestrictedMemberAccess(null);
            }
            instanceService.save(instance);
            return createResponse(mapInstance(instance, user));
        }
        throw new NotFoundException();
    }


    @DELETE
    @Path("/{instance}")
    public MetaResponse<InstanceDto> delete(@Context final SecurityContext securityContext, @PathParam("instance") final Instance instance) {
        final User user = this.getUserPrincipal(securityContext);
        if (instance.getComputeId() == null || instance.hasAnyState(List.of(InstanceState.STOPPED, InstanceState.ERROR, InstanceState.UNKNOWN, InstanceState.UNAVAILABLE))) {
            return this.performAction(instance, user, InstanceCommandType.DELETE);

        } else {
            instance.setDeleteRequested(true);
            this.instanceService.save(instance);
            if (instance.getState().equals(InstanceState.STOPPING)) {
                return createResponse(mapInstance(instance, user));

            } else {
                return this.performAction(instance, user, InstanceCommandType.SHUTDOWN);
            }
        }
    }

    @GET
    @Path("/{instance}/experiments/team")
    public MetaResponse<List<UserDto>> getTeam(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance) {
        final User user = this.getUserPrincipal(securityContext);
        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            List<User> team = userService.getExperimentalTeamForInstance(instance);
            if (clientConfiguration.experimentsConfiguration().openDataIncluded()) {
                // Only return the team if the owner is part of it
                if (!team.contains(user)) {
                    team = new ArrayList<>();
                }
            }

            return createResponse(team.stream().map(this::mapUser).collect(toList()));
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/{instance}/members")
    public MetaResponse<List<InstanceMemberDto>> allMembers(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance) {
        final User user = this.getUserPrincipal(securityContext);

        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            return createResponse(instance.getMembers().stream()
                .map(InstanceMemberDto::new)
                .sorted()
                .collect(toList()));
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/{instance}/sessions/active/members")
    public MetaResponse<List<InstanceSessionMemberDto>> allActiveSessionMembers(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance) {
        final User user = this.getUserPrincipal(securityContext);

        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            List<InstanceSessionMember> sessionMembers = this.instanceSessionService.getAllSessionMembersByInstance(instance);

            return createResponse(sessionMembers.stream()
                .peek(instanceSessionMember -> instanceSessionMember.getInstanceSession().setInstance(instance))
                .map(InstanceSessionMemberDto::new)
                .collect(toList()));
        }

        throw new NotFoundException();
    }


    @POST
    @Path("/{instance}/auth/token")
    public RestResponse<MetaResponse<InstanceAuthenticationTokenDto>> createInstanceAuthenticationTicket(@Context final SecurityContext securityContext, @PathParam("instance") Instance instance) {
        final User user = this.getUserPrincipal(securityContext);
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
    public MetaResponse<InstanceMemberDto> addMember(@Context final SecurityContext securityContext,
                                                     @PathParam("instance") final Instance instance,
                                                     @Valid @NotNull final InstanceMemberCreatorDto instanceMemberCreatorDto) {
        final User user = this.getUserPrincipal(securityContext);
        if (this.instanceService.isOwnerOrAdmin(user, instance)) {
            User memberUser = userService.getById(instanceMemberCreatorDto.getUserId());
            if (memberUser != null) {
                InstanceMember instanceMember = InstanceMember.newBuilder()
                    .user(memberUser)
                    .role(instanceMemberCreatorDto.getRole())
                    .build();

                instance.addMember(instanceMember);

                this.instanceService.save(instance);

                emailManager.sendInstanceMemberAddedNotification(instance, instanceMember);

                return createResponse(new InstanceMemberDto(instanceMember));

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
    public MetaResponse<InstanceMemberDto> updateMember(@Context final SecurityContext securityContext,
                                                        @PathParam("instance") Instance instance,
                                                        @PathParam("member") InstanceMember instanceMember,
                                                        @Valid @NotNull final InstanceMemberUpdatorDto memberUpdateDto) {
        final User user = this.getUserPrincipal(securityContext);
        if (this.instanceService.isOwnerOrAdmin(user, instance)) {
            if (instance.isMember(instanceMember)) {
                if (memberUpdateDto.isRole(OWNER)) {
                    throw new BadRequestException("Cannot make a member an owner. There can only be one owner.");
                }
                instanceMember.setRole(memberUpdateDto.getRole());
                instanceMemberService.save(instanceMember);
                return createResponse(new InstanceMemberDto(instanceMember));
            }
        }
        throw new NotFoundException();
    }

    @DELETE
    @Path("/{instance}/members/{member}")
    public void removeMember(@Context final SecurityContext securityContext,
                                             @PathParam("instance") Instance instance,
                                             @PathParam("member") InstanceMember instanceMember) {
        final User user = this.getUserPrincipal(securityContext);
        if (this.instanceService.isOwnerOrAdmin(user, instance)) {
            if (instance.isMember(instanceMember) && !instanceMember.isRole(OWNER)) {
                instance.removeMember(instanceMember);

                instanceService.save(instance);
            }

        } else if (instance.isMember(user)) {
            throw new NotAuthorizedException("Not authorized to perform this action");
        }

        throw new NotFoundException();
    }

    @GET
    @Path("/{instance}/history")
    public MetaResponse<List<InstanceCommandDto>> getHistory(@Context final SecurityContext securityContext,
                                                             @PathParam("instance") Instance instance) {
        final User user = this.getUserPrincipal(securityContext);
        if (this.instanceService.isOwnerOrAdmin(user, instance)) {
            List<InstanceCommand> commands = instanceCommandService.getAllForInstance(instance);

            List<InstanceCommandDto> history = commands.stream()
                .map(InstanceCommandDto::new)
                .collect(toList());

            return createResponse(history);

        } else if (instance.isMember(user)) {
            throw new NotAuthorizedException("Not authorized to perform this action");
        }

        throw new NotFoundException();
    }

    private MetaResponse<InstanceDto> performAction(final Instance instance, final User user, final InstanceCommandType instanceCommandType) {
        if (this.instanceService.isOwnerOrAdmin(user, instance)) {
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
        InstanceDto instanceDto = new InstanceDto(instance);
        InstanceMember instanceMember = instanceMemberService.getByInstanceAndUser(instance, user);
        if (instanceMember != null) {
            instanceDto.setMembership(new InstanceMemberDto(instanceMember));

        } else if (user.hasRole(Role.ADMIN_ROLE)) {
            instanceDto.setMembership(new InstanceMemberDto(this.mapUser(user), SUPPORT));

        } else if (user.hasAnyRole(List.of(Role.IT_SUPPORT_ROLE, Role.INSTRUMENT_CONTROL_ROLE, Role.INSTRUMENT_SCIENTIST_ROLE))) {
            instanceDto.setMembership(new InstanceMemberDto(this.mapUser(user), SUPPORT));
        }
        instanceDto.setCanConnectWhileOwnerAway(instanceSessionService.canConnectWhileOwnerAway(instance, user.getId()));
        instanceDto.setUnrestrictedAccess((instance.getUnrestrictedMemberAccess() != null));

        return instanceDto;
    }

    private UserDto mapUser(User user) {
        return new UserDto(user);
    }

    @POST
    @Path("/{instance}/thumbnail")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void createThumbnail(@Context final SecurityContext securityContext,
                                @PathParam("instance") final Instance instance,
                                @NotNull @RestForm("file") final InputStream is) {
        try {
            final User user = this.getUserPrincipal(securityContext);
            if (this.instanceService.isOwnerOrAdmin(user, instance)) {
                final byte[] data = is.readAllBytes();
                final ImageFormat mimeType = Imaging.guessFormat(data);
                if (mimeType == ImageFormats.JPEG) {
                    instanceService.createOrUpdateThumbnail(instance, data);
                }
            }

        } catch (Exception exception) {
            logger.error("Error creating thumbnail for instance: {}", instance.getId(), exception);
        }
    }

    @GET
    @Path("/{instance}/thumbnail")
    @Produces("image/jpeg")
    public Response getThumbnail(@Context final SecurityContext securityContext,
                                 @PathParam("instance") final Instance instance) {
        final User user = this.getUserPrincipal(securityContext);
        if (this.instanceService.isOwnerOrAdmin(user, instance)) {
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

    @GET
    @Path("/{instance}/extension")
    public MetaResponse<InstanceExtensionRequestDto> getInstanceExtensionRequest(@Context final SecurityContext securityContext,
                              @PathParam("instance") final Instance instance) {
        final User user = this.getUserPrincipal(securityContext);
        if (this.instanceService.isAuthorisedForInstance(user, instance)) {
            InstanceExtensionRequest request = this.instanceExtensionRequestService.getForInstance(instance);
            if (request != null) {
                InstanceExtensionRequestDto requestDto = new InstanceExtensionRequestDto(request);
                return createResponse(requestDto);

            } else {
                return createResponse();
            }

        } else{
            throw new NotAuthorizedException("Not authorized to perform this action");
        }
    }

    @POST
    @Path("/{instance}/extension")
    public MetaResponse<InstanceExtensionRequestDto> createInstanceExtensionRequest(@Context final SecurityContext securityContext,
                              @PathParam("instance") final Instance instance,
                              @Valid @NotNull final InstanceExtensionRequestInput instanceExtensionRequestInput) {
        final User user = this.getUserPrincipal(securityContext);
        if (this.instanceService.isOwnerOrAdmin(user, instance)) {

            // Check an existing request hasn't already been made
            InstanceExtensionRequest request = this.instanceExtensionRequestService.getForInstance(instance);
            if (request == null) {
                request = this.instanceExtensionRequestService.create(instance, instanceExtensionRequestInput.getComments());
            }
            InstanceExtensionRequestDto requestDto = new InstanceExtensionRequestDto(request);
            return createResponse(requestDto);

        } else{
            throw new NotAuthorizedException("Not authorized to perform this action");
        }
    }

}
