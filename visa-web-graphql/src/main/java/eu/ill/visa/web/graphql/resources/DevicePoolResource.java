package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.business.services.CloudProviderService;
import eu.ill.visa.business.services.DevicePoolService;
import eu.ill.visa.business.services.FlavourService;
import eu.ill.visa.cloud.domain.CloudDevice;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.core.entity.DevicePool;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.DevicePoolInput;
import eu.ill.visa.web.graphql.types.DevicePoolType;
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

import java.util.Date;
import java.util.List;

import static eu.ill.visa.business.tools.CloudDeviceTypeConverter.toCloudDeviceType;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class DevicePoolResource {

    private static final Logger logger = LoggerFactory.getLogger(DevicePoolResource.class);

    private final DevicePoolService devicePoolService;
    private final FlavourService flavourService;
    private final CloudClientService cloudClientService;
    private final CloudProviderService cloudProviderService;

    @Inject
    public DevicePoolResource(final DevicePoolService devicePoolService,
                              final FlavourService flavourService,
                              final CloudClientService cloudClientService,
                              final CloudProviderService cloudProviderService) {
        this.devicePoolService = devicePoolService;
        this.flavourService = flavourService;
        this.cloudClientService = cloudClientService;
        this.cloudProviderService = cloudProviderService;
    }

    /**
     * Get a list of device pools
     *
     * @return a list of device pools
     */
    @Query
    public @NotNull List<DevicePoolType> devicePools() {
        return this.devicePoolService.getAll().stream()
            .map(DevicePoolType::new)
            .toList();
    }

    /**
     * Create a new device pool
     *
     * @param input the device pool properties
     * @return the newly created device pool
     */
    @Mutation
    public @NotNull DevicePoolType createDevicePool(@NotNull @Valid DevicePoolInput input) throws InvalidInputException {
        // Validate the devicePool input

        // Verify we don't already have a device with the same characteristics
        final DevicePool existingDevicePool = this.devicePoolService.getComputeIdentifierAndType(input.getComputeIdentifier(), input.getDeviceType());
        if (existingDevicePool != null) {
            throw new InvalidInputException("Device pool already exists");
        }

        this.validateDevicePoolInput(input);
        final DevicePool devicePool = new DevicePool();
        this.mapToDevicePool(input, devicePool);
        devicePoolService.create(devicePool);

        // Update the flavour device pools
        this.flavourService.updateAllFlavourDevicePools();

        return new DevicePoolType(devicePool);
    }

    /**
     * Update a device pool
     *
     * @param id    the device pool id
     * @param input the device pool properties
     * @return the updated created device pool
     * @throws EntityNotFoundException thrown if the given the device pool id was not found
     */
    @Mutation
    public @NotNull DevicePoolType updateDevicePool(@NotNull @AdaptToScalar(Scalar.Int.class) Long id, @NotNull @Valid DevicePoolInput input) throws EntityNotFoundException, InvalidInputException  {
        // Verify we don't already have a device with the same characteristics
        final DevicePool existingDevicePool = this.devicePoolService.getComputeIdentifierAndType(input.getComputeIdentifier(), input.getDeviceType());
        if (existingDevicePool != null && !existingDevicePool.getId().equals(id)) {
            throw new InvalidInputException("Device pool already exists");
        }

        // Validate the devicePool input
        this.validateDevicePoolInput(input);

        final DevicePool devicePool = this.devicePoolService.getById(id);
        if (devicePool == null) {
            throw new EntityNotFoundException("Device pool was not found for the given id");
        }
        this.mapToDevicePool(input, devicePool);
        devicePoolService.save(devicePool);

        // Update the flavour device pools
        this.flavourService.updateAllFlavourDevicePools();

        return new DevicePoolType(devicePool);
    }

    /**
     * Delete a device pool for a given id
     *
     * @param id the device pool id
     * @return the deleted device pool
     * @throws EntityNotFoundException thrown if the device pool is not found
     */
    @Mutation
    public @NotNull DevicePoolType deleteDevicePool(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException {
        final DevicePool devicePool = devicePoolService.getById(id);
        if (devicePool == null) {
            throw new EntityNotFoundException("Device pool not found for the given id");
        }
        devicePool.setDeletedAt(new Date());
        devicePoolService.save(devicePool);

        // Update the flavour device pools
        this.flavourService.updateAllFlavourDevicePools();

        return new DevicePoolType(devicePool);
    }

    private void mapToDevicePool(DevicePoolInput input, DevicePool devicePool) {
        devicePool.setName(input.getName());
        devicePool.setDescription(input.getDescription());
        devicePool.setDeviceType(input.getDeviceType());
        devicePool.setComputeIdentifier(input.getComputeIdentifier());
        devicePool.setTotalUnits(input.getTotalUnits());
        devicePool.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));
    }

    private void validateDevicePoolInput(DevicePoolInput devicePoolInput) throws InvalidInputException {
        try {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(devicePoolInput.getCloudId());
            if (cloudClient == null) {
                throw new InvalidInputException("Invalid cloud Id");
            }

            List<CloudDevice> cloudDevices = cloudClient.devices();
            CloudDevice cloudDevice = cloudDevices.stream()
                .filter(device -> device.getIdentifier().equals(devicePoolInput.getComputeIdentifier()))
                .filter(device -> device.getType().equals(toCloudDeviceType(devicePoolInput.getDeviceType())))
                .findFirst().orElse(null);

            if (cloudDevice == null) {
                throw new InvalidInputException("Invalid Cloud Device details");
            }

        } catch (CloudException exception) {
            throw new InvalidInputException("Error accessing Cloud");
        }
    }

    private CloudProviderConfiguration getCloudProviderConfiguration(Long cloudId) {
        if (cloudId != null && cloudId > 0) {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(cloudId);
            return this.cloudProviderService.getById(cloudClient.getId());
        }
        return null;
    }


}
