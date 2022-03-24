package eu.ill.visa.web.bundles.graphql.queries.resolvers;

import com.google.inject.Inject;
import eu.ill.visa.business.services.*;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.bundles.graphql.context.AuthenticationContext;
import eu.ill.visa.web.bundles.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.bundles.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.bundles.graphql.exceptions.ValidationException;
import eu.ill.visa.web.bundles.graphql.queries.domain.Message;
import eu.ill.visa.web.bundles.graphql.queries.inputs.*;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import org.apache.bval.guice.Validate;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static eu.ill.visa.web.bundles.graphql.queries.domain.Message.createMessage;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class MutationResolver implements GraphQLMutationResolver {

    private final static Logger logger = LoggerFactory.getLogger(MutationResolver.class);

    private final Mapper                     mapper;
    private final FlavourService             flavourService;
    private final InstanceService            instanceService;
    private final InstanceExpirationService  instanceExpirationService;
    private final ImageService               imageService;
    private final PlanService                planService;
    private final FlavourLimitService        flavourLimitService;
    private final SecurityGroupService       securityGroupService;
    private final SecurityGroupFilterService securityGroupFilterService;
    private final InstrumentService          instrumentService;

    private final InstanceActionScheduler   instanceActionScheduler;
    private final RoleService               roleService;
    private final UserService               userService;
    private final ImageProtocolService      imageProtocolService;
    private final SystemNotificationService systemNotificationService;

    @Inject
    public MutationResolver(final Mapper mapper,
                            final FlavourService flavourService,
                            final InstanceService instanceService,
                            final InstanceExpirationService instanceExpirationService,
                            final ImageService imageService,
                            final PlanService planService,
                            final FlavourLimitService flavourLimitService,
                            final SecurityGroupService securityGroupService,
                            final SecurityGroupFilterService securityGroupFilterService,
                            final InstrumentService instrumentService,
                            final InstanceActionScheduler instanceActionScheduler,
                            final RoleService roleService,
                            final UserService userService,
                            final ImageProtocolService imageProtocolService,
                            final SystemNotificationService systemNotificationService) {
        this.mapper = mapper;
        this.flavourService = flavourService;
        this.instanceService = instanceService;
        this.instanceExpirationService = instanceExpirationService;
        this.imageService = imageService;
        this.planService = planService;
        this.flavourLimitService = flavourLimitService;
        this.securityGroupService = securityGroupService;
        this.securityGroupFilterService = securityGroupFilterService;
        this.instrumentService = instrumentService;
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
    public Image createImage(@Valid ImageInput input) throws EntityNotFoundException {
        final Image image = new Image();
        image.setName(input.getName());
        image.setVersion(input.getVersion());
        image.setDescription(input.getDescription());
        image.setIcon(input.getIcon());
        image.setComputeId(input.getComputeId());
        image.setVisible(input.getVisible());
        image.setDeleted(false);
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
    public Image updateImage(Long id, @Valid ImageInput input) throws EntityNotFoundException {
        final Image image = imageService.getById(id);
        image.setName(input.getName());
        image.setVersion(input.getVersion());
        image.setDescription(input.getDescription());
        image.setIcon(input.getIcon());
        image.setComputeId(input.getComputeId());
        image.setVisible(input.getVisible());
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
     * Delete a image for a given id
     *
     * @param id the image id
     * @return the deleted flavour
     * @throws EntityNotFoundException thrown if the image is not found
     */
    public Image deleteImage(Long id) throws EntityNotFoundException {
        final Image image = imageService.getById(id);
        if (image == null) {
            throw new EntityNotFoundException("Image not found for the given id");
        }
        image.setDeleted(true);
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
    public Flavour createFlavour(@Valid FlavourInput input) {
        final Flavour flavour = mapper.map(input, Flavour.class);
        flavourService.create(flavour);

        // Handle flavour limits
        this.updateFlavourLimits(flavour, input);

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
    public Flavour updateFlavour(Long id, @Valid FlavourInput input) throws EntityNotFoundException {
        final Flavour flavour = this.flavourService.getById(id);
        if (flavour == null) {
            throw new EntityNotFoundException("Flavour was not found for the given id");
        }
        flavour.setName(input.getName());
        flavour.setComputeId(input.getComputeId());
        flavour.setCpu(input.getCpu());
        flavour.setMemory(input.getMemory());
        flavourService.save(flavour);

        // Handle flavour limits
        this.updateFlavourLimits(flavour, input);

        return flavour;
    }

    private void updateFlavourLimits(Flavour flavour, FlavourInput input) {
        List<FlavourLimit> currentFlavourLimits = this.flavourLimitService.getAllOfTypeForFlavour(flavour, "INSTRUMENT");
        List<Long> currentInstrumentIds = currentFlavourLimits.stream().map(FlavourLimit::getObjectId).collect(Collectors.toList());
        List<Long> instrumentIds = input.getInstrumentIds();
        List<Long> allInstrumentIds = this.instrumentService.getAll().stream().map(Instrument::getId).collect(Collectors.toList());

        List<FlavourLimit> flavourLimitsToDelete = currentFlavourLimits.stream().filter(flavourLimit ->
            !instrumentIds.contains(flavourLimit.getObjectId())).collect(Collectors.toList());
        List<Long> instrumentsToAdd = instrumentIds.stream().filter(instrumentId ->
            !currentInstrumentIds.contains(instrumentId)).collect(Collectors.toList());

        flavourLimitsToDelete.forEach(flavourLimit -> this.flavourLimitService.delete(flavourLimit));
        instrumentsToAdd.forEach(instrumentId -> {
            if (allInstrumentIds.contains(instrumentId)) {
                FlavourLimit flavourLimit = new FlavourLimit(flavour, instrumentId, "INSTRUMENT");
                this.flavourLimitService.save(flavourLimit);

            } else {
                logger.warn("Cannot create FlavourLimit with instrument Id {} for Flavour {} as it does not exist", instrumentId, flavour.getName());
            }
        });
    }

    /**
     * Delete a flavour for a given id
     *
     * @param id the flavour id
     * @return the deleted flavour
     * @throws EntityNotFoundException thrown if the flavour is not found
     */
    public Flavour deleteFlavour(Long id) throws EntityNotFoundException {
        final Flavour flavour = flavourService.getById(id);
        if (flavour == null) {
            throw new EntityNotFoundException("Flavour not found for the given id");
        }
        flavour.setDeleted(true);
        flavourService.save(flavour);
        return flavour;
    }

    /**
     * Create a new securityGroup
     *
     * @param input the securityGroup name
     * @return the newly created securityGroup
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public SecurityGroup createSecurityGroup(String input) {
        final SecurityGroup securityGroup = new SecurityGroup(input);
        securityGroupService.save(securityGroup);
        return securityGroup;
    }

    /**
     * Update a securityGroup
     *
     * @param id    the securityGroup id
     * @param input the securityGroup name
     * @return the updated created securityGroup
     * @throws EntityNotFoundException thrown if the given the securityGroup id was not found
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public SecurityGroup updateSecurityGroup(Long id, String input) throws EntityNotFoundException {
        final SecurityGroup securityGroup = this.securityGroupService.getById(id);
        if (securityGroup == null) {
            throw new EntityNotFoundException("SecurityGroup was not found for the given id");
        }

        // TODO Check that security group exists

        securityGroup.setName(input);

        securityGroupService.save(securityGroup);
        return securityGroup;
    }

    /**
     * Delete a security group for a given id
     *
     * @param id the security group id
     * @return the deleted security group
     * @throws EntityNotFoundException thrown if the security group is not found
     */
    public SecurityGroup deleteSecurityGroup(Long id) throws EntityNotFoundException {
        final SecurityGroup securityGroup = securityGroupService.getById(id);
        if (securityGroup == null) {
            throw new EntityNotFoundException("Security group not found for the given id");
        }
        securityGroupFilterService.getAll()
            .stream()
            .filter(filter -> filter.getSecurityGroup().getId().equals(id))
            .forEach(securityGroupFilterService::delete);
        securityGroupService.delete(securityGroup);
        return securityGroup;
    }

    /**
     * Create a new securityGroupFilter
     *
     * @param input the securityGroupFilter properties
     * @return the newly created securityGroupFilter
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public SecurityGroupFilter createSecurityGroupFilter(@Valid SecurityGroupFilterInput input) throws InvalidInputException {
        if (securityGroupFilterService.securityGroupFilterBySecurityIdAndObjectIdAndType(input.getSecurityGroupId(), input.getObjectId(), input.getObjectType()) == null) {
            final SecurityGroup securityGroup = securityGroupService.getById(input.getSecurityGroupId());
            if (securityGroup == null) {
                throw new InvalidInputException("Security group does not exist");
            }
            final SecurityGroupFilter securityGroupFilter = new SecurityGroupFilter(securityGroup, input.getObjectId(), input.getObjectType());
            securityGroupFilterService.save(securityGroupFilter);
            return securityGroupFilter;
        }
        throw new InvalidInputException("A security group filter for the given object id and type already exists");
    }

    /**
     * Update a securityGroupFilter
     *
     * @param id    the securityGroupFilter id
     * @param input the securityGroupFilter properties
     * @return the updated created securityGroupFilter
     * @throws EntityNotFoundException thrown if the given the securityGroupFilter id was not found
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public SecurityGroupFilter updateSecurityGroupFilter(Long id, @Valid SecurityGroupFilterInput input) throws EntityNotFoundException, InvalidInputException {
        final SecurityGroupFilter securityGroupFilter = this.securityGroupFilterService.getById(id);
        if (securityGroupFilter == null) {
            throw new EntityNotFoundException("SecurityGroupFilter was not found for the given id");
        }

        final SecurityGroup securityGroup = this.securityGroupService.getById(input.getSecurityGroupId());
        if (securityGroup == null) {
            throw new EntityNotFoundException("SecurityGroup not found for the given id");
        }

        String objectType = input.getObjectType();
        final String[] validObjectTypes = {"INSTRUMENT", "ROLE"};
        if (!Arrays.asList(validObjectTypes).contains(objectType)) {
            throw new InvalidInputException("ObjectType is not valid for SecurityGroupFilter");
        }

        // Check objectIds are valid
        if (objectType.equals("INSTRUMENT") && instrumentService.getById(input.getObjectId()) == null) {
            throw new EntityNotFoundException("Instrument not found for the given id");

        } else if (objectType.equals("ROLE") && roleService.getById(input.getObjectId()) == null) {
            throw new EntityNotFoundException("Role not found for the given id");
        }

        securityGroupFilter.setSecurityGroup(securityGroup);
        securityGroupFilter.setObjectId(input.getObjectId());
        securityGroupFilter.setObjectType(input.getObjectType());
        securityGroupFilterService.save(securityGroupFilter);
        return securityGroupFilter;
    }

    /**
     * Delete a securityGroupFilter for a given id
     *
     * @param id the securityGroupFilter id
     * @return the deleted securityGroupFilter
     * @throws EntityNotFoundException thrown if the securityGroupFilter is not found
     */
    public SecurityGroupFilter deleteSecurityGroupFilter(Long id) throws EntityNotFoundException {
        final SecurityGroupFilter securityGroupFilter = securityGroupFilterService.getById(id);
        if (securityGroupFilter == null) {
            throw new EntityNotFoundException("SecurityGroupFilter not found for the given id");
        }
        securityGroupFilterService.delete(securityGroupFilter);
        return securityGroupFilter;
    }

    /**
     * Create a new plan
     *
     * @param input the plan properties
     * @return the newly created plan
     * @throws EntityNotFoundException thrown if the given flavour is not found
     * @throws EntityNotFoundException thrown if the given image is not found
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public Plan createPlan(@Valid PlanInput input) throws EntityNotFoundException {
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
     * @throws EntityNotFoundException thrown if the given flavour is not found
     * @throws EntityNotFoundException thrown if the given image is not found
     * @throws EntityNotFoundException thrown if the given plan is not found
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public Plan updatePlan(Long id, @Valid PlanInput input) throws EntityNotFoundException {
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
     * @throws EntityNotFoundException thrown if there are instances associated to the plan
     */
    public Plan deletePlan(Long id) throws EntityNotFoundException {
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


    /**
     * Reboot an instance
     *
     * @param id          the instance id
     * @param environment the graphql environment
     * @return a message
     * @throws EntityNotFoundException thrown if the instance has not been found
     */
    public Message rebootInstance(Long id, DataFetchingEnvironment environment) throws EntityNotFoundException {
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
    public Message startInstance(Long id, DataFetchingEnvironment environment) throws EntityNotFoundException {
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
    public Message shutdownInstance(Long id, DataFetchingEnvironment environment) throws EntityNotFoundException {
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
    public Message deleteInstance(Long id, DataFetchingEnvironment environment) throws EntityNotFoundException {
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
     * Update an instance termination date
     *
     * @param id         the instance id
     * @param dateString the instance termination date
     * @return a message
     * @throws EntityNotFoundException thrown if the instance has not been found
     * @throws ValidationException     thrown if date can't be parsed
     */
    public Message updateInstanceTerminationDate(Long id, String dateString) throws EntityNotFoundException, ValidationException {
        final Instance instance = instanceService.getById(id);

        if (instance == null) {
            throw new EntityNotFoundException("Instance not found for the given id");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        try {
            Date terminationDate = simpleDateFormat.parse(dateString);
            instance.setTerminationDate(terminationDate);
            this.instanceService.save(instance);

            // Delete any existing expirations
            InstanceExpiration expiration = this.instanceExpirationService.getByInstance(instance);
            if (expiration != null) {
                this.instanceExpirationService.delete(expiration);
            }

        } catch (ParseException e) {
            throw new ValidationException(e);
        }

        return createMessage("Instance termination date has been updated");
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
     * Create a new systemNotification
     *
     * @param input the systemNotification properties
     * @return the newly created systemNotification
     */
    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public SystemNotification createSystemNotification(@Valid SystemNotificationInput input) {
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
    public SystemNotification updateSystemNotification(Long id, @Valid SystemNotificationInput input) throws EntityNotFoundException, InvalidInputException {
        final SystemNotification systemNotification = this.systemNotificationService.getById(id);
        if (systemNotification == null) {
            throw new EntityNotFoundException("systemNotification not found for the given id");
        }
        systemNotification.setLevel(input.getLevel());
        systemNotification.setMessage(input.getMessage());

        try {
            systemNotification.setActivatedAt(input.getActivatedAt() == null ? null : SystemNotificationInput.DATE_FORMAT.parse(input.getActivatedAt()));
        } catch (ParseException e) {
            throw new InvalidInputException("The activation date does not have a coherent format");
        }
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
    public SystemNotification deleteSystemNotification(Long id) throws EntityNotFoundException {
        final SystemNotification systemNotification = systemNotificationService.getById(id);
        if (systemNotification == null) {
            throw new EntityNotFoundException("systemNotification not found for the given id");
        }
        systemNotificationService.delete(systemNotification);
        return systemNotification;
    }

    /**
     * Updates a user's role
     *
     * @param userId    the user ID
     * @param roleName  the role name
     * @param isEnabled if the role is to be added or not
     * @return the user
     * @throws EntityNotFoundException thrown if the user or role has not been found
     */
    public User updateUserRole(String userId, String roleName, boolean isEnabled) throws EntityNotFoundException {

        final User user = userService.getById(userId);
        if (user == null) {
            throw new EntityNotFoundException("User not found for the given user id");
        }
        final Role role = roleService.getByName(roleName);
        if (role == null) {
            throw new EntityNotFoundException("Role not found for the given role name");
        }

        if (isEnabled) {
            user.addRole(role);
        } else {
            user.removeRole(role);
        }

        this.userService.save(user);

        return user;
    }

}
