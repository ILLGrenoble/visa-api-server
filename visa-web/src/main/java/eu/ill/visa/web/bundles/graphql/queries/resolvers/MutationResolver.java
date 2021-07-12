package eu.ill.visa.web.bundles.graphql.queries.resolvers;

import com.google.inject.Inject;
import eu.ill.visa.business.services.*;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.bundles.graphql.context.AuthenticationContext;
import eu.ill.visa.web.bundles.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.bundles.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.bundles.graphql.exceptions.ValidationException;
import eu.ill.visa.web.bundles.graphql.queries.domain.Message;
import eu.ill.visa.web.bundles.graphql.queries.inputs.*;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import org.apache.bval.guice.Validate;
import org.dozer.Mapper;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static eu.ill.visa.web.bundles.graphql.queries.domain.Message.createMessage;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class MutationResolver implements GraphQLMutationResolver {

    private final Mapper mapper;
    private final FlavourService flavourService;
    private final InstanceService instanceService;
    private final ImageService imageService;
    private final PlanService planService;

    private final InstanceActionScheduler instanceActionScheduler;
    private final RoleService roleService;
    private final UserService userService;
    private final ImageProtocolService imageProtocolService;
    private final SystemNotificationService systemNotificationService;

    @Inject
    public MutationResolver(final Mapper mapper,
                            final FlavourService flavourService,
                            final InstanceService instanceService,
                            final ImageService imageService,
                            final PlanService planService,
                            final InstanceActionScheduler instanceActionScheduler,
                            final RoleService roleService,
                            final UserService userService,
                            final ImageProtocolService imageProtocolService,
                            final SystemNotificationService systemNotificationService) {
        this.mapper = mapper;
        this.flavourService = flavourService;
        this.instanceService = instanceService;
        this.imageService = imageService;
        this.planService = planService;
        this.instanceActionScheduler = instanceActionScheduler;
        this.roleService = roleService;
        this.userService = userService;
        this.imageProtocolService = imageProtocolService;
        this.systemNotificationService = systemNotificationService;
    }

    /**
     * Create a new image
     *
     * @param input the image properties
     * @return the newly created image
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    Image createImage(@Valid CreateImageInput input) throws EntityNotFoundException {
        final Image image = new Image();
        image.setName(input.getName());
        image.setVersion(input.getVersion());
        image.setDescription(input.getDescription());
        image.setIcon(input.getIcon());
        image.setComputeId(input.getComputeId());
        image.setVisible(input.getVisible());
        image.setDeleted(input.getDeleted());
        image.setBootCommand(input.getBootCommand());
        image.setAutologin(input.getAutologin());
        final List<Long> protocolsId = input.getProtocolIds();
        final List<ImageProtocol> protocols = new ArrayList<>();
        for (Long protocolId : protocolsId) {
            final ImageProtocol protocol = imageProtocolService.getById(protocolId);
            if (protocol == null) {
                throw new EntityNotFoundException("Protocol not found for the given id");
            }
            protocols.add(protocol);
        }
        image.setProtocols(protocols);
        // final Image image = mapper.map(input, Image.class);
        imageService.save(image);
        return image;
    }

    /**
     * Update a new image
     *
     * @param id    the image id
     * @param input the image properties
     * @return the newly created image
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    Image updateImage(Long id, @Valid UpdateImageInput input) throws EntityNotFoundException {
        final Image image = imageService.getById(id);
        image.setName(input.getName());
        image.setVersion(input.getVersion());
        image.setDescription(input.getDescription());
        image.setIcon(input.getIcon());
        image.setComputeId(input.getComputeId());
        image.setVisible(input.getVisible());
        image.setDeleted(input.getDeleted());
        image.setBootCommand(input.getBootCommand());
        image.setAutologin(input.getAutologin());
        final List<Long> protocolsId = input.getProtocolIds();
        final List<ImageProtocol> protocols = new ArrayList<>();
        for (Long protocolId : protocolsId) {
            final ImageProtocol protocol = imageProtocolService.getById(protocolId);
            if (protocol == null) {
                throw new EntityNotFoundException("Protocol not found for the given id");
            }
            protocols.add(protocol);
        }
        image.setProtocols(protocols);
        imageService.save(image);
        return image;
    }

    /**
     * Create a new flavour
     *
     * @param input the flavour properties
     * @return the newly created flavour
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    Flavour createFlavour(@Valid CreateFlavourInput input) {
        final Flavour flavour = mapper.map(input, Flavour.class);
        flavourService.create(flavour);
        return flavour;
    }

    /**
     * Update a flavour
     *
     * @param id    the flavour id
     * @param input the flavour properties
     * @return the updated created flavour
     * @throws EntityNotFoundException thrown if the given the flavour id was not found
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    Flavour updateFlavour(Long id, @Valid UpdateFlavourInput input) throws EntityNotFoundException {
        final Flavour flavour = this.flavourService.getById(id);
        if(flavour == null) {
            throw new EntityNotFoundException("Flavour was not found for the given id");
        }
        flavour.setName(input.getName());
        flavour.setComputeId(input.getComputeId());
        flavour.setCpu(input.getCpu());
        flavour.setMemory(input.getMemory());
        flavourService.save(flavour);
        return flavour;
    }

    /**
     * Delete a flavour for a given id
     *
     * @param id the flavour id
     * @return the deleted flavour
     * @throws EntityNotFoundException thrown if the flavour is not found
     * @throws DataFetchingException   thrown if there are instances associated to the flavour
     */
    Flavour deleteFlavour(Long id) throws EntityNotFoundException, DataFetchingException {
        final Flavour flavour = flavourService.getById(id);
        if (flavour == null) {
            throw new EntityNotFoundException("Flavour not found for the given id");
        }
        flavour.setDeleted(true);
        flavourService.save(flavour);
        return flavour;
    }

    /**
     * Delete a image for a given id
     *
     * @param id the image id
     * @return the deleted flavour
     * @throws EntityNotFoundException thrown if the image is not found
     */
    Image deleteImage(Long id) throws EntityNotFoundException {
        final Image image = imageService.getById(id);
        if (image == null) {
            throw new EntityNotFoundException("Image not found for the given id");
        }
        image.setDeleted(true);
        imageService.save(image);
        return image;
    }

    /**
     * Create a new plan
     *
     * @param input the plan properties
     * @return the newly created plan
     * @throws EntityNotFoundException   thrown if the given flavour is not found
     * @throws EntityNotFoundException   thrown if the given image is not found
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    Plan createPlan(@Valid CreatePlanInput input) throws EntityNotFoundException {
        final Flavour flavour = this.flavourService.getById(input.getFlavourId());
        if (flavour == null) {
            throw new EntityNotFoundException("Flavour not found for the given id");
        }
        final Image image = this.imageService.getById(input.getImageId());
        if (image == null) {
            throw new EntityNotFoundException("Image not found for the given id");
        }
        if (input.getPreset()) {
            // reset all plans to preset of false
            planService.getAllForAdmin().forEach(object -> {
                object.setPreset(false);
                planService.save(object);
            });
        }
        final Plan plan = new Plan();
        plan.setFlavour(flavour);
        plan.setImage(image);
        plan.setPreset(input.getPreset());
        planService.create(plan);
        return plan;
    }

    /**
     * Update a plan
     *
     * @param id    the plan id
     * @param input the plan properties
     * @return the updated plan
     * @throws EntityNotFoundException   thrown if the given flavour is not found
     * @throws EntityNotFoundException   thrown if the given image is not found
     * @throws EntityNotFoundException   thrown if the given plan is not found
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    Plan updatePlan(Long id, @Valid UpdatePlanInput input) throws EntityNotFoundException {
        final Flavour flavour = this.flavourService.getById(input.getFlavourId());
        if (flavour == null) {
            throw new EntityNotFoundException("Flavour not found for the given id");
        }
        final Image image = this.imageService.getById(input.getImageId());
        if (image == null) {
            throw new EntityNotFoundException("Image not found for the given id");
        }
        final Plan plan = planService.getById(id);
        if (plan == null) {
            throw new EntityNotFoundException("Plan not found for the given id");
        }

        if (input.getPreset()) {
            // reset all plans to preset of false
            planService.getAllForAdmin().forEach(object -> {
                object.setPreset(false);
                planService.save(object);
            });
        }
        plan.setFlavour(flavour);
        plan.setImage(image);
        plan.setPreset(input.getPreset());
        planService.save(plan);
        return plan;
    }

    /**
     * Delete a plan for a given id
     *
     * @param id the plan id
     * @return the deleted plan
     * @throws EntityNotFoundException thrown if the plan is not found
     * @throws EntityNotFoundException   thrown if there are instances associated to the plan
     *
     */
    Plan deletePlan(Long id) throws EntityNotFoundException {
        final Plan plan = planService.getById(id);
        if (plan == null) {
            throw new EntityNotFoundException("Plan not found for the given id");
        }
        final List<Parameter> parameters = singletonList(new Parameter("id", plan.getId().toString()));
        final QueryFilter filter = new QueryFilter("plan.id = :id", parameters);
        final Long countInstances = instanceService.countAll(filter);
        if (countInstances > 0) {
            throw new EntityNotFoundException(format("Cannot delete this plan because there are %d instances associated to it", countInstances));
        }
        planService.delete(plan);
        return plan;
    }


    InstanceAuthenticationToken createInstanceAuthenticationToken(Long id, DataFetchingEnvironment environment) throws EntityNotFoundException {
        final AuthenticationContext context = environment.getContext();
        final AccountToken token = context.getAccountToken();
        final User user = token.getUser();
        final Instance instance = instanceService.getById(id);
        if (instance == null) {
            throw new EntityNotFoundException("Instance not found for the given id");
        }
        // @TODO Create a token even if not a member of desired instance i.e. admin should be able to create a token even if they are not a member
        return null;
    }

    /**
     * Reboot an instance
     *
     * @param id          the instance id
     * @param environment the graphql environment
     * @return a message
     * @throws EntityNotFoundException thrown if the instance has not been found
     */
    Message rebootInstance(Long id, DataFetchingEnvironment environment) throws EntityNotFoundException {
        performInstanceAction(id, InstanceCommandType.REBOOT, environment);
        return createMessage("Instance will be rebooted");
    }

    /**
     * Start an instance
     *
     * @param id          the instance id
     * @param environment the graphql environment
     * @return a message
     * @throws EntityNotFoundException thrown if the instance has not been found
     */
    Message startInstance(Long id, DataFetchingEnvironment environment) throws EntityNotFoundException {
        performInstanceAction(id, InstanceCommandType.START, environment);
        return createMessage("Instance will be started");
    }

    /**
     * Shutdown an instance
     *
     * @param id          the instance id
     * @param environment the graphql environment
     * @return a message
     * @throws EntityNotFoundException thrown if the instance has not been found
     */
    Message shutdownInstance(Long id, DataFetchingEnvironment environment) throws EntityNotFoundException {
        performInstanceAction(id, InstanceCommandType.SHUTDOWN, environment);
        return createMessage("Instance will be shutdown");
    }

    /**
     * Delete an instance
     *
     * @param id          the instance id
     * @param environment the graphql environment
     * @return a message
     * @throws EntityNotFoundException thrown if the instance has not been found
     */
    Message deleteInstance(Long id, DataFetchingEnvironment environment) throws EntityNotFoundException {
        final Instance instance = instanceService.getById(id);

        if (instance == null) {
            throw new EntityNotFoundException("Instance not found for the given id");
        }

        if (instance.getComputeId() == null || instance.hasAnyState(List.of(InstanceState.STOPPED, InstanceState.ERROR, InstanceState.UNKNOWN, InstanceState.UNAVAILABLE))) {
            this.performInstanceAction(id, InstanceCommandType.DELETE, environment);
        } else {
            instance.setDeleteRequested(true);
            this.instanceService.save(instance);
            this.performInstanceAction(id, InstanceCommandType.SHUTDOWN, environment);
        }
        return createMessage("Instance is scheduled for deletion");
    }

    /**
     * Execute an instance action
     *
     * @param id                  the id of the instance
     * @param instanceCommandType the command type to schedule
     * @param environment         the graphql environment
     * @throws EntityNotFoundException thrown if the instance has not been found
     */
    private void performInstanceAction(final Long id, final InstanceCommandType instanceCommandType, DataFetchingEnvironment environment) throws EntityNotFoundException {
        final AuthenticationContext context = environment.getContext();
        final AccountToken token = context.getAccountToken();
        final User user = token.getUser();
        final Instance instance = instanceService.getById(id);

        if (instance == null) {
            throw new EntityNotFoundException("Instance not found for the given id");
        }

        instanceActionScheduler.execute(instance, user, instanceCommandType);
    }




    /**
     * Create a new role
     *
     * @param input the role properties
     * @return the newly created role
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    Role createRole(@Valid CreateRoleInput input) {
        final Role role = mapper.map(input, Role.class);
        roleService.create(role);
        return role;
    }

    /**
     * Delete a role for a given id
     *
     * @param id the role id
     * @return the deleted role
     * @throws EntityNotFoundException thrown if the role is not found
     * @throws DataFetchingException   thrown if there are users associated to the role
     */
    Role deleteRole(Long id) throws EntityNotFoundException, DataFetchingException {
        final Role role = roleService.getById(id);
        if (role == null) {
            throw new EntityNotFoundException("Role not found for the given id");
        }
        final List<Parameter> parameters = singletonList(new Parameter("name", role.getName()));
        final QueryFilter filter = new QueryFilter("role = :name", parameters);
        final Long countUsers = userService.countAll(filter);
        if (countUsers > 0) {
            throw new DataFetchingException(format("Cannot delete this role because there are %d users associated to it", countUsers));
        }
        roleService.delete(role);
        return role;
    }

    /**
     * Create a new systemNotification
     *
     * @param input the systemNotification properties
     * @return the newly created systemNotification
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    SystemNotification createSystemNotification(@Valid CreateSystemNotificationInput input){
        final SystemNotification systemNotification = mapper.map(input, SystemNotification.class);
        systemNotificationService.save(systemNotification);
        return systemNotification;
    }

    /**
     * Update a new systemNotification
     *
     * @param input the systemNotification properties
     * @return the updated systemNotification
     * @throws EntityNotFoundException thrown if the systemNotification has not been found
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    SystemNotification updateSystemNotification(Long id,@Valid UpdateSystemNotificationInput input) throws EntityNotFoundException {
        final SystemNotification systemNotification = this.systemNotificationService.getById(id);
        if(systemNotification == null) {
            throw new EntityNotFoundException("systemNotification not found for the given id");
        }
        systemNotification.setLevel(input.getLevel());
        systemNotification.setMessage(input.getMessage());
        systemNotificationService.save(systemNotification);
        return systemNotification;
    }

    /**
     * Delete a systemNotification
     *
     * @param id the instance id
     * @return a notification
     * @throws EntityNotFoundException thrown if the instance has not been found
     */
    SystemNotification deleteSystemNotification(Long id) throws EntityNotFoundException {
        final SystemNotification systemNotification = systemNotificationService.getById(id);
        if (systemNotification == null) {
            throw new EntityNotFoundException("systemNotification not found for the given id");
        }
        systemNotificationService.delete(systemNotification);
        return systemNotification;
    }

}