package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.*;
import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.FlavourInput;
import eu.ill.visa.web.graphql.types.FlavourType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class FlavourResource {

    private static final Logger logger = LoggerFactory.getLogger(FlavourResource.class);

    private final FlavourService flavourService;
    private final FlavourLimitService flavourLimitService;
    private final InstrumentService instrumentService;
    private final RoleService roleService;
    private final CloudClientGateway cloudClientGateway;
    private final CloudProviderService cloudProviderService;

    @Inject
    public FlavourResource(final FlavourService flavourService,
                           final FlavourLimitService flavourLimitService,
                           final InstrumentService instrumentService,
                           final RoleService roleService,
                           final CloudClientGateway cloudClientGateway,
                           final CloudProviderService cloudProviderService) {
        this.flavourService = flavourService;
        this.flavourLimitService = flavourLimitService;
        this.instrumentService = instrumentService;
        this.roleService = roleService;
        this.cloudClientGateway = cloudClientGateway;
        this.cloudProviderService = cloudProviderService;
    }

    /**
     * Get a list of flavours
     *
     * @return a list of flavours
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public @NotNull List<FlavourType> flavours() throws DataFetchingException {
        try {

            return this.flavourService.getAllForAdmin().stream()
            .map(FlavourType::new)
            .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }


    /**
     * Count all flavours
     *
     * @return a count of images
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countFlavours() throws DataFetchingException {
        try {
            return flavourService.countAllForAdmin();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }


    /**
     * Create a new flavour
     *
     * @param input the flavour properties
     * @return the newly created flavour
     */
    @Mutation
    public @NotNull FlavourType createFlavour(@NotNull @Valid FlavourInput input) throws InvalidInputException {
        // Validate the flavour input
        this.validateFlavourInput(input);
        final Flavour flavour = new Flavour();
        this.mapToFlavour(input, flavour);
        flavourService.create(flavour);

        // Handle flavour limits
        this.updateFlavourLimits(flavour, input);

        return new FlavourType(flavour);
    }

    /**
     * Update a flavour
     *
     * @param id    the flavour id
     * @param input the flavour properties
     * @return the updated created flavour
     * @throws EntityNotFoundException thrown if the given the flavour id was not found
     */
    @Mutation
    public @NotNull FlavourType updateFlavour(@NotNull Long id, @NotNull @Valid FlavourInput input) throws EntityNotFoundException, InvalidInputException  {
        // Validate the flavour input
        this.validateFlavourInput(input);

        final Flavour flavour = this.flavourService.getById(id);
        if (flavour == null) {
            throw new EntityNotFoundException("Flavour was not found for the given id");
        }
        this.mapToFlavour(input, flavour);
        flavourService.save(flavour);

        // Handle flavour limits
        this.updateFlavourLimits(flavour, input);

        return new FlavourType(flavour);
    }

    /**
     * Delete a flavour for a given id
     *
     * @param id the flavour id
     * @return the deleted flavour
     * @throws EntityNotFoundException thrown if the flavour is not found
     */
    @Mutation
    public @NotNull FlavourType deleteFlavour(@NotNull Long id) throws EntityNotFoundException {
        final Flavour flavour = flavourService.getById(id);
        if (flavour == null) {
            throw new EntityNotFoundException("Flavour not found for the given id");
        }
        flavour.setDeleted(true);
        flavourService.save(flavour);
        return new FlavourType(flavour);
    }

    private void mapToFlavour(FlavourInput input, Flavour flavour) {
        flavour.setName(input.getName());
        flavour.setMemory(input.getMemory());
        flavour.setCpu(input.getCpu());
        flavour.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));
        flavour.setComputeId(input.getComputeId());
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


    private void updateFlavourLimits(Flavour flavour, FlavourInput input) {
        // Handle instrument limits
        List<FlavourLimit> currentInstrumentFlavourLimits = this.flavourLimitService.getAllOfTypeForFlavour(flavour, "INSTRUMENT");
        List<Long> currentInstrumentIds = currentInstrumentFlavourLimits.stream().map(FlavourLimit::getObjectId).toList();
        List<Long> instrumentIds = input.getInstrumentIds();
        List<Long> allInstrumentIds = this.instrumentService.getAll().stream().map(Instrument::getId).collect(Collectors.toList());

        List<FlavourLimit> instrumentFlavourLimitsToDelete = currentInstrumentFlavourLimits.stream().filter(flavourLimit ->
            !instrumentIds.contains(flavourLimit.getObjectId())).toList();
        this.deleteFlavourLimits(instrumentFlavourLimitsToDelete);

        List<Long> instrumentsToAdd = instrumentIds.stream().filter(instrumentId -> !currentInstrumentIds.contains(instrumentId)).collect(Collectors.toList());
        this.createFlavourLimits(instrumentsToAdd, allInstrumentIds, flavour, "INSTRUMENT");

        // Handle role limits
        List<FlavourLimit> currentRoleFlavourLimits = this.flavourLimitService.getAllOfTypeForFlavour(flavour, "ROLE");
        List<Long> currentRoleIds = currentRoleFlavourLimits.stream().map(FlavourLimit::getObjectId).toList();
        List<Long> roleIds = input.getRoleIds();
        List<Long> allRoleIds = this.roleService.getAllRolesAndGroups().stream().map(Role::getId).collect(Collectors.toList());

        List<FlavourLimit> roleFlavourLimitsToDelete = currentRoleFlavourLimits.stream().filter(flavourLimit ->
            !roleIds.contains(flavourLimit.getObjectId())).toList();

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

    private CloudProviderConfiguration getCloudProviderConfiguration(Long cloudId) {
        if (cloudId != null && cloudId > 0) {
            CloudClient cloudClient = this.cloudClientGateway.getCloudClient(cloudId);
            return this.cloudProviderService.getById(cloudClient.getId());
        }
        return null;
    }


}
