package eu.ill.visa.web.graphql.queries.resolvers;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import eu.ill.visa.business.services.*;
import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientFactory;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.domain.NumberInstancesByCloudClient;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import eu.ill.visa.core.entity.enumerations.InstanceExtensionRequestState;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.graphql.context.AuthenticationContext;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.exceptions.ValidationException;
import eu.ill.visa.web.graphql.queries.domain.ApplicationCredentialDetail;
import eu.ill.visa.web.graphql.queries.domain.Message;
import eu.ill.visa.web.graphql.queries.inputs.*;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static eu.ill.visa.web.graphql.queries.domain.Message.createMessage;

@ApplicationScoped
public class MutationResolver implements GraphQLMutationResolver {

    private final static Logger logger = LoggerFactory.getLogger(MutationResolver.class);

    private final Mapper                     mapper;
    private final FlavourService             flavourService;
    private final InstanceService            instanceService;
    private final ImageService               imageService;
    private final PlanService                planService;
    private final FlavourLimitService        flavourLimitService;
    private final SecurityGroupService       securityGroupService;
    private final SecurityGroupFilterService securityGroupFilterService;
    private final InstrumentService          instrumentService;
    private final CloudClientGateway         cloudClientGateway;
    private final CloudProviderService       cloudProviderService;

    private final InstanceActionScheduler      instanceActionScheduler;
    private final RoleService                  roleService;
    private final UserService                  userService;
    private final ImageProtocolService         imageProtocolService;
    private final ClientNotificationService    clientNotificationService;
    private final ApplicationCredentialService applicationCredentialService;

    private final InstanceExtensionRequestService   instanceExtensionRequestService;


    @Inject
    public MutationResolver(final FlavourService flavourService,
                            final InstanceService instanceService,
                            final ImageService imageService,
                            final PlanService planService,
                            final FlavourLimitService flavourLimitService,
                            final SecurityGroupService securityGroupService,
                            final SecurityGroupFilterService securityGroupFilterService,
                            final InstrumentService instrumentService,
                            final CloudClientGateway cloudClientGateway,
                            final CloudProviderService cloudProviderService,
                            final InstanceActionScheduler instanceActionScheduler,
                            final RoleService roleService,
                            final UserService userService,
                            final ImageProtocolService imageProtocolService,
                            final ClientNotificationService clientNotificationService,
                            final ApplicationCredentialService applicationCredentialService,
                            final InstanceExtensionRequestService instanceExtensionRequestService) {
        this.mapper = DozerBeanMapperBuilder.create().build();
        this.flavourService = flavourService;
        this.instanceService = instanceService;
        this.imageService = imageService;
        this.planService = planService;
        this.flavourLimitService = flavourLimitService;
        this.securityGroupService = securityGroupService;
        this.securityGroupFilterService = securityGroupFilterService;
        this.instrumentService = instrumentService;
        this.cloudClientGateway = cloudClientGateway;
        this.cloudProviderService = cloudProviderService;
        this.instanceActionScheduler = instanceActionScheduler;
        this.roleService = roleService;
        this.userService = userService;
        this.imageProtocolService = imageProtocolService;
        this.clientNotificationService = clientNotificationService;
        this.applicationCredentialService = applicationCredentialService;
        this.instanceExtensionRequestService = instanceExtensionRequestService;
    }

    /**
     * Create a new image
     *
     * @param input the image properties
     * @return the newly created image
     */
    public Image createImage(@Valid ImageInput input) throws EntityNotFoundException, InvalidInputException  {
        // Validate the image input
        this.validateImageInput(input);

        final Image image = new Image();
        image.setName(input.getName());
        image.setVersion(input.getVersion());
        image.setDescription(input.getDescription());
        image.setIcon(input.getIcon());
        image.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));
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
    public Image updateImage(Long id, @Valid ImageInput input) throws EntityNotFoundException, InvalidInputException  {
        // Validate the image input
        this.validateImageInput(input);

        final Image image = imageService.getById(id);
        image.setName(input.getName());
        image.setVersion(input.getVersion());
        image.setDescription(input.getDescription());
        image.setIcon(input.getIcon());
        image.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));
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

    private void validateImageInput(ImageInput imageInput) throws InvalidInputException {
        try {
            CloudClient cloudClient = this.cloudClientGateway.getCloudClient(imageInput.getCloudId());
            if (cloudClient == null) {
                throw new InvalidInputException("Invalid cloud Id");
            }

            CloudImage cloudImage = cloudClient.image(imageInput.getComputeId());
            if (cloudImage == null) {
                throw new InvalidInputException("Invalid Cloud Image Id");
            }

        } catch (CloudException exception) {
            throw new InvalidInputException("Error accessing Cloud");
        }
    }

    /**
     * Create a new flavour
     *
     * @param input the flavour properties
     * @return the newly created flavour
     */
    public Flavour createFlavour(@Valid FlavourInput input) throws InvalidInputException {
        // Validate the flavour input
        this.validateFlavourInput(input);

        final Flavour flavour = mapper.map(input, Flavour.class);
        flavour.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));
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
    public Flavour updateFlavour(Long id, @Valid FlavourInput input) throws EntityNotFoundException, InvalidInputException  {
        // Validate the flavour input
        this.validateFlavourInput(input);

        final Flavour flavour = this.flavourService.getById(id);
        if (flavour == null) {
            throw new EntityNotFoundException("Flavour was not found for the given id");
        }
        flavour.setName(input.getName());
        flavour.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));
        flavour.setComputeId(input.getComputeId());
        flavour.setCpu(input.getCpu());
        flavour.setMemory(input.getMemory());
        flavourService.save(flavour);

        // Handle flavour limits
        this.updateFlavourLimits(flavour, input);

        return flavour;
    }

    private void updateFlavourLimits(Flavour flavour, FlavourInput input) {
        // Handle instrument limits
        List<FlavourLimit> currentInstrumentFlavourLimits = this.flavourLimitService.getAllOfTypeForFlavour(flavour, "INSTRUMENT");
        List<Long> currentInstrumentIds = currentInstrumentFlavourLimits.stream().map(FlavourLimit::getObjectId).collect(Collectors.toList());
        List<Long> instrumentIds = input.getInstrumentIds();
        List<Long> allInstrumentIds = this.instrumentService.getAll().stream().map(Instrument::getId).collect(Collectors.toList());

        List<FlavourLimit> instrumentFlavourLimitsToDelete = currentInstrumentFlavourLimits.stream().filter(flavourLimit ->
            !instrumentIds.contains(flavourLimit.getObjectId())).collect(Collectors.toList());
        this.deleteFlavourLimits(instrumentFlavourLimitsToDelete);

        List<Long> instrumentsToAdd = instrumentIds.stream().filter(instrumentId -> !currentInstrumentIds.contains(instrumentId)).collect(Collectors.toList());
        this.createFlavourLimits(instrumentsToAdd, allInstrumentIds, flavour, "INSTRUMENT");

        // Handle role limits
        List<FlavourLimit> currentRoleFlavourLimits = this.flavourLimitService.getAllOfTypeForFlavour(flavour, "ROLE");
        List<Long> currentRoleIds = currentRoleFlavourLimits.stream().map(FlavourLimit::getObjectId).collect(Collectors.toList());
        List<Long> roleIds = input.getRoleIds();
        List<Long> allRoleIds = this.roleService.getAllRolesAndGroups().stream().map(Role::getId).collect(Collectors.toList());

        List<FlavourLimit> roleFlavourLimitsToDelete = currentRoleFlavourLimits.stream().filter(flavourLimit ->
            !roleIds.contains(flavourLimit.getObjectId())).collect(Collectors.toList());

        this.deleteFlavourLimits(roleFlavourLimitsToDelete);
        List<Long> rolesToAdd = roleIds.stream().filter(roleId -> !currentRoleIds.contains(roleId)).collect(Collectors.toList());
        this.createFlavourLimits(rolesToAdd, allRoleIds, flavour, "ROLE");
    }

    private void deleteFlavourLimits(List<FlavourLimit> flavourLimits) {
        flavourLimits.forEach(this.flavourLimitService::delete);
    }

    private void createFlavourLimits(List<Long> objectIds, List<Long> validObjectIds, Flavour flavour, String type) {
        objectIds.forEach(objectId -> {
            if (validObjectIds.contains(objectId)) {
                FlavourLimit flavourLimit = new FlavourLimit(flavour, objectId, type);
                this.flavourLimitService.save(flavourLimit);

            } else {
                logger.warn("Cannot create FlavourLimit with {} Id {} for Flavour {} as it does not exist", type, objectId, flavour.getName());
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

    private void validateFlavourInput(FlavourInput flavourInput) throws InvalidInputException {
        try {
            CloudClient cloudClient = this.cloudClientGateway.getCloudClient(flavourInput.getCloudId());
            if (cloudClient == null) {
                throw new InvalidInputException("Invalid cloud Id");
            }

            CloudFlavour cloudFlavour = cloudClient.flavour(flavourInput.getComputeId());
            if (cloudFlavour == null) {
                throw new InvalidInputException("Invalid Cloud Flavour Id");
            }

        } catch (CloudException exception) {
            throw new InvalidInputException("Error accessing Cloud");
        }
    }

    /**
     * Create a new securityGroup
     *
     * @param input the securityGroup name
     * @return the newly created securityGroup
     */
    public SecurityGroup createSecurityGroup(SecurityGroupInput input) throws InvalidInputException {
        // Validate the security group
        this.validateSecurityGroupInput(input);

        Optional<SecurityGroup> existingSecurityGroup = this.securityGroupService.getAll()
            .stream()
            .filter(securityGroup -> {
                if (!input.getName().equals(securityGroup.getName())) {
                    return false;
                }
                Long inputCloudId = input.getCloudId() == -1 ? null :  input.getCloudId();
                if (inputCloudId == null) {
                    return securityGroup.getCloudId() == null;
                } else {
                    return inputCloudId.equals(securityGroup.getCloudId());
                }
            })
            .findFirst();
        if (existingSecurityGroup.isPresent()) {
            return existingSecurityGroup.get();
        }

        final SecurityGroup securityGroup = new SecurityGroup(input.getName());
        securityGroup.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));
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
    public SecurityGroup updateSecurityGroup(Long id, SecurityGroupInput input) throws EntityNotFoundException, InvalidInputException {
        final SecurityGroup securityGroup = this.securityGroupService.getById(id);
        if (securityGroup == null) {
            throw new EntityNotFoundException("SecurityGroup was not found for the given id");
        }

        // Validate the security group
        this.validateSecurityGroupInput(input);

        securityGroup.setName(input.getName());
        securityGroup.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));

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

    private void validateSecurityGroupInput(SecurityGroupInput securityGroupInput) throws InvalidInputException {
        try {
            CloudClient cloudClient = this.cloudClientGateway.getCloudClient(securityGroupInput.getCloudId());
            if (cloudClient == null) {
                throw new InvalidInputException("Invalid cloud Id");
            }

            boolean securityGroupExists = cloudClient.securityGroups().contains(securityGroupInput.getName());
            if (!securityGroupExists) {
                throw new InvalidInputException("Invalid Cloud Security Group");
            }

        } catch (CloudException exception) {
            throw new InvalidInputException("Error accessing Cloud");
        }
    }


    private CloudProviderConfiguration getCloudProviderConfiguration(Long cloudId) {
        if (cloudId != null && cloudId > 0) {
            CloudClient cloudClient = this.cloudClientGateway.getCloudClient(cloudId);
            return this.cloudProviderService.getById(cloudClient.getId());
        }
        return null;
    }

    /**
     * Create a new securityGroupFilter
     *
     * @param input the securityGroupFilter properties
     * @return the newly created securityGroupFilter
     */
    public SecurityGroupFilter createSecurityGroupFilter(@Valid SecurityGroupFilterInput input) throws EntityNotFoundException, InvalidInputException {
        if (securityGroupFilterService.securityGroupFilterBySecurityIdAndObjectIdAndType(input.getSecurityGroupId(), input.getObjectId(), input.getObjectType()) == null) {

            // Validate the input data
            SecurityGroup securityGroup = this.validateSecurityGroupFilterInput(input);

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
    public SecurityGroupFilter updateSecurityGroupFilter(Long id, @Valid SecurityGroupFilterInput input) throws EntityNotFoundException, InvalidInputException {
        final SecurityGroupFilter securityGroupFilter = this.securityGroupFilterService.getById(id);
        if (securityGroupFilter == null) {
            throw new EntityNotFoundException("SecurityGroupFilter was not found for the given id");
        }

        // Validate the input data
        SecurityGroup securityGroup = this.validateSecurityGroupFilterInput(input);

        securityGroupFilter.setSecurityGroup(securityGroup);
        securityGroupFilter.setObjectId(input.getObjectId());
        securityGroupFilter.setObjectType(input.getObjectType());
        securityGroupFilterService.save(securityGroupFilter);
        return securityGroupFilter;
    }

    private SecurityGroup validateSecurityGroupFilterInput(SecurityGroupFilterInput input) throws EntityNotFoundException, InvalidInputException {

        final SecurityGroup securityGroup = this.securityGroupService.getById(input.getSecurityGroupId());
        if (securityGroup == null) {
            throw new EntityNotFoundException("SecurityGroup not found for the given id");
        }

        String objectType = input.getObjectType();
        final String[] validObjectTypes = {"INSTRUMENT", "ROLE", "FLAVOUR"};
        if (!Arrays.asList(validObjectTypes).contains(objectType)) {
            throw new InvalidInputException("ObjectType is not valid for SecurityGroupFilter");
        }

        // Check objectIds are valid
        if (objectType.equals("INSTRUMENT") && instrumentService.getById(input.getObjectId()) == null) {
            throw new EntityNotFoundException("Instrument not found for the given id");

        } else if (objectType.equals("ROLE") && roleService.getById(input.getObjectId()) == null) {
            throw new EntityNotFoundException("Role not found for the given id");

        } else if (objectType.equals("FLAVOUR") && flavourService.getById(input.getObjectId()) == null) {
            throw new EntityNotFoundException("Flavour not found for the given id");
        }

        return securityGroup;
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
//    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
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
//    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
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
        plan.setDeleted(true);
        planService.save(plan);
        return plan;
    }


    /**
     * Create a new cloudClient
     *
     * @param input the cloudClient properties
     * @return the newly created cloudClient
     */
//    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public CloudClient createCloudClient(@Valid CloudClientInput input) throws InvalidInputException {
        CloudProviderConfiguration.Builder builder = new CloudProviderConfiguration.Builder();
        CloudProviderConfiguration configuration = builder
            .type(input.getType())
            .name(input.getName())
            .visible(input.getVisible())
            .serverNamePrefix(input.getServerNamePrefix())
            .build();

        this.setCloudConfigurationParameters(configuration, input);

        return this.cloudProviderService.createCloudClient(configuration);
    }

    /**
     * Update a cloudClient
     *
     * @param id    the cloudClient id
     * @param input the cloudClient properties
     * @return the updated created cloudClient
     * @throws EntityNotFoundException thrown if the given the cloudClient id was not found
     */
//    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public CloudClient updateCloudClient(Long id, @Valid CloudClientInput input) throws EntityNotFoundException, InvalidInputException  {
        if (id == -1) {
            throw new InvalidInputException("The default cloud provider cannot be modified");
        }

        CloudProviderConfiguration configuration = this.cloudProviderService.getById(id);
        if (configuration == null) {
            throw new EntityNotFoundException("Cloud provider not found for the given id");
        }

        if (!configuration.getType().equals(input.getType())) {
            configuration.deleteParameters();
        }

        configuration.setName(input.getName());
        configuration.setServerNamePrefix(input.getServerNamePrefix());
        configuration.setType(input.getType());
        configuration.setVisible(input.getVisible());

        this.setCloudConfigurationParameters(configuration, input);

        return this.cloudProviderService.save(configuration);
    }

    private void setCloudConfigurationParameters(CloudProviderConfiguration cloudProviderConfiguration, CloudClientInput input) throws InvalidInputException {
        if (input.getType().equals(CloudClientFactory.OPENSTACK)) {
            if (input.getOpenStackProviderConfiguration() == null) {
                throw new InvalidInputException("OpenStack provider configuration must be specified");
            }
            OpenStackProviderConfigurationInput configurationInput = input.getOpenStackProviderConfiguration();

            cloudProviderConfiguration.setParameter("applicationId", configurationInput.getApplicationId());
            cloudProviderConfiguration.setParameter("applicationSecret", configurationInput.getApplicationSecret());
            cloudProviderConfiguration.setParameter("computeEndpoint", configurationInput.getComputeEndpoint());
            cloudProviderConfiguration.setParameter("imageEndpoint", configurationInput.getImageEndpoint());
            cloudProviderConfiguration.setParameter("networkEndpoint", configurationInput.getNetworkEndpoint());
            cloudProviderConfiguration.setParameter("identityEndpoint", configurationInput.getIdentityEndpoint());
            cloudProviderConfiguration.setParameter("addressProvider", configurationInput.getAddressProvider());
            cloudProviderConfiguration.setParameter("addressProviderUUID", configurationInput.getAddressProviderUUID());

        } else if (input.getType().equals(CloudClientFactory.WEB)) {
            if (input.getWebProviderConfiguration() == null) {
                throw new InvalidInputException("Web provider configuration must be specified");
            }

            WebProviderConfigurationInput configurationInput = input.getWebProviderConfiguration();
            cloudProviderConfiguration.setParameter("url", configurationInput.getUrl());
            cloudProviderConfiguration.setParameter("authToken", configurationInput.getAuthToken());

        } else {
            throw new InvalidInputException("Cloud provider type must be specified");
        }
    }

    /**
     * Delete a cloudClient for a given id
     *
     * @param id the cloudClient id
     * @return true if deleted
     * @throws EntityNotFoundException thrown if the cloudClient is not found
     * @throws InvalidInputException thrown if trying to delete the default cloud client
     */
    public Boolean deleteCloudClient(Long id) throws EntityNotFoundException, InvalidInputException {
        if (id == -1) {
            throw new InvalidInputException("The default cloud client cannot be deleted");
        }

        final CloudProviderConfiguration configuration = this.cloudProviderService.getById(id);
        if (configuration == null) {
            throw new EntityNotFoundException("Cloud Client not found for the given id");
        }

        NumberInstancesByCloudClient counter = this.instanceService.countByCloudClient().stream().filter(count -> {
            if (count.getId() == null) {
                return id == null || id == -1L;
            } else {
                return count.getId().equals(id);
            }
        }).findFirst().orElse(null);
        if (counter != null && counter.getTotal() > 0) {
            throw new InvalidInputException("Cannot delete a cloud provider with active instances");
        }

        this.cloudProviderService.delete(configuration);
        return true;
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
            Date terminationDate = dateString == null ? null : simpleDateFormat.parse(dateString);

            this.instanceExtensionRequestService.grantExtension(instance, terminationDate, null, false);

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
//    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public SystemNotification createSystemNotification(@Valid SystemNotificationInput input) {
        final SystemNotification systemNotification = mapper.map(input, SystemNotification.class);
        clientNotificationService.saveSystemNotification(systemNotification);
        return systemNotification;
    }

    /**
     * Update a new systemNotification
     *
     * @param input the systemNotification properties
     * @return the updated systemNotification
     * @throws EntityNotFoundException thrown if the systemNotification has not been found
     */
//    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public SystemNotification updateSystemNotification(Long id, @Valid SystemNotificationInput input) throws EntityNotFoundException, InvalidInputException {
        final SystemNotification systemNotification = this.clientNotificationService.getSystemNotificationById(id);
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
        clientNotificationService.saveSystemNotification(systemNotification);
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
        final SystemNotification systemNotification = clientNotificationService.getSystemNotificationById(id);
        if (systemNotification == null) {
            throw new EntityNotFoundException("systemNotification not found for the given id");
        }
        clientNotificationService.deleteSystemNotification(systemNotification);
        return systemNotification;
    }

    public Role createRole(RoleInput input) throws InvalidInputException {
        final Role existingRole = this.roleService.getByName(input.getName());
        if (existingRole != null) {
            throw new InvalidInputException("Role with given name already exists");
        }

        Role role = new Role(input.getName(), input.getDescription());
        role.setGroupCreatedAt(new Date());

        this.roleService.save(role);

        return role;
    }

    public Role updateRole(Long roleId, RoleInput input) throws EntityNotFoundException, InvalidInputException {
        final Role existingRoleWithId = this.roleService.getById(roleId);
        if (existingRoleWithId == null) {
            throw new EntityNotFoundException("Role with given Id does not exist");
        }

        final Role existingRoleWithName = this.roleService.getByName(input.getName());
        if (existingRoleWithName != null && !existingRoleWithName.getId().equals(roleId)) {
            throw new InvalidInputException("Role with given name already exists");
        }
        existingRoleWithId.setName(input.getName());
        existingRoleWithId.setDescription(input.getDescription());

        this.roleService.save(existingRoleWithId);

        return existingRoleWithId;
    }

    public Boolean deleteRole(Long roleId) throws EntityNotFoundException {
        final Role existingRoleWithId = this.roleService.getById(roleId);
        if (existingRoleWithId == null) {
            throw new EntityNotFoundException("Role with given Id does not exist");
        }

        existingRoleWithId.setGroupDeletedAt(new Date());
        this.roleService.save(existingRoleWithId);

        return true;
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

    /**
     * Create a new application credential
     *
     * @param input the application credential properties
     * @return the newly created application credential
     */
//    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public ApplicationCredential createApplicationCredential(@Valid ApplicationCredentialInput input) {
        final ApplicationCredential applicationCredential = applicationCredentialService.create(input.getName());
        return applicationCredential;
    }

    /**
     * Update an application credential
     *
     * @param input the application credential properties
     * @return the updated application credential
     * @throws EntityNotFoundException thrown if the applicationCredential has not been found
     */
//    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public ApplicationCredentialDetail updateApplicationCredential(Long id, @Valid ApplicationCredentialInput input) throws EntityNotFoundException {
        final ApplicationCredential applicationCredential = this.applicationCredentialService.getById(id);
        if (applicationCredential == null) {
            throw new EntityNotFoundException("application credential not found for the given id");
        }
        applicationCredential.setName(input.getName());

        applicationCredentialService.save(applicationCredential);
        return new ApplicationCredentialDetail(applicationCredential);
    }

    /**
     * Delete an application credential
     *
     * @param id of the application credential
     * @return the deleted application credential
     * @throws EntityNotFoundException thrown if the application credential has not been found
     */
    public ApplicationCredentialDetail deleteApplicationCredential(Long id) throws EntityNotFoundException {
        final ApplicationCredential applicationCredential = applicationCredentialService.getById(id);
        if (applicationCredential == null) {
            throw new EntityNotFoundException("applicationCredential not found for the given id");
        }
        applicationCredentialService.delete(applicationCredential);
        return new ApplicationCredentialDetail(applicationCredential);
    }

    /**
     * Update a user
     *
     * @param id the user id to update
     * @param input the user properties to update
     * @return the updated user
     * @throws EntityNotFoundException thrown if the applicationCredential has not been found
     */
//    @Validate(rethrowExceptionsAs = ValidationException.class, validateReturnedValue = true)
    public User updateUser(String id, @Valid UserInput input) throws EntityNotFoundException {
        final User user = userService.getById(id);
        if (user == null) {
            throw new EntityNotFoundException("User not found for the given user id");
        }

        final Role adminRole = roleService.getByName("ADMIN");
        final Role guestRole = roleService.getByName("GUEST");

        // Filter input groups, remove any that do not exist
        List<Role> inputGroups = input.getGroupIds().stream().map(roleService::getById).filter(Objects::nonNull).collect(Collectors.toList());

        // Get all current groups of the user
        List<Role> userGroups = user.getGroups();

        // Determine which ones to add and to remove
        List<Role> rolesToAdd = inputGroups.stream().filter(role -> !userGroups.contains(role)).collect(Collectors.toList());
        List<Role> rolesToRemove =  userGroups.stream().filter(userRole -> !inputGroups.contains(userRole)).collect(Collectors.toList());

        try {
            if (input.getAdmin()) {
                user.addRole(adminRole);
            } else {
                user.removeRole(adminRole);
            }

            if (input.getGuest()) {
                Date guestExpiresAt = input.getGuestExpiresAt() == null ? null : UserInput.DATE_FORMAT.parse(input.getGuestExpiresAt());
                user.addRole(guestRole, guestExpiresAt);
            } else {
                user.removeRole(guestRole);
            }

            for (Role role : rolesToAdd) {
                user.addRole(role);
            }

            for (Role role : rolesToRemove) {
                user.removeRole(role);
            }

            user.setInstanceQuota(input.getInstanceQuota());
            this.userService.save(user);
            return user;

        } catch (ParseException e) {
            throw new ValidationException(e);
        }
    }

    public InstanceExtensionRequest handleInstanceExtensionRequest(Long requestId, @Valid InstanceExtensionResponseInput response) throws EntityNotFoundException {
        InstanceExtensionRequest request = this.instanceExtensionRequestService.getById(requestId);
        if (request == null) {
            throw new EntityNotFoundException("Extension request not found for the given id");
        }
        User user = this.userService.getById(response.getHandlerId());
        if (user == null) {
            throw new EntityNotFoundException("The user who handled the request could not be found");
        }

        try {
            request.setState(response.getAccepted() ? InstanceExtensionRequestState.ACCEPTED : InstanceExtensionRequestState.REFUSED);
            request.setHandledOn(new Date());
            request.setHandler(user);
            request.setHandlerComments(response.getHandlerComments());
            if (response.getAccepted()) {
                Date terminationDate = InstanceExtensionResponseInput.DATE_FORMAT.parse(response.getTerminationDate());
                request.setExtensionDate(terminationDate);

                this.instanceExtensionRequestService.grantExtension(request.getInstance(), terminationDate, response.getHandlerComments(), true);

            } else {
                this.instanceExtensionRequestService.refuseExtension(request.getInstance(), response.getHandlerComments());
            }

            // Update the request
            this.instanceExtensionRequestService.save(request);

            return request;

        } catch (ParseException e) {
            throw new ValidationException(e);
        }

    }


}
