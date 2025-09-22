package eu.ill.visa.business.services;

import eu.ill.visa.cloud.domain.CloudDevice;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.DevicePool;
import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.core.entity.FlavourDevice;
import eu.ill.visa.persistence.repositories.FlavourRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static eu.ill.visa.business.tools.CloudDeviceTypeConverter.fromCloudDeviceType;

@Transactional
@Singleton
public class FlavourService {
    private static final Logger logger = LoggerFactory.getLogger(FlavourService.class);

    private final FlavourRepository repository;
    private final DevicePoolService devicePoolService;
    private final CloudClientService cloudClientService;

    @Inject
    public FlavourService(final FlavourRepository repository,
                          final DevicePoolService devicePoolService,
                          final CloudClientService cloudClientService) {
        this.repository = repository;
        this.devicePoolService = devicePoolService;
        this.cloudClientService = cloudClientService;
    }

    public List<Flavour> getAll() {
        return this.repository.getAll();
    }

    public List<Flavour> getAllForAdmin() {
        return this.repository.getAllForAdmin();
    }

    public Flavour getById(Long id) {
        return this.repository.getById(id);
    }

    public void save(@NotNull Flavour flavour) {
        this.repository.save(flavour);
    }

    public void create(Flavour flavour) {
        this.repository.create(flavour);
    }

    public Long countAllForAdmin() {
        return repository.countAllForAdmin();
    }

    public void updateAllFlavourDevicePools() {
        final List<Flavour> flavours = this.repository.getAllForAdmin();
        for (final Flavour flavour : flavours) {
            this.updateFlavourDevicePools(flavour);
        }
    }

    public void updateFlavourDevicePools(final Flavour flavour) {
        logger.info("Updating flavour {} (id = {}) device pools", flavour.getName(), flavour.getId());

        final List<DevicePool> allDevicePools = this.devicePoolService.getAll();
        try {
            final CloudClient cloudClient = this.cloudClientService.getCloudClient(flavour.getCloudId());

            final List<FlavourDevice> requiredDevices = cloudClient.flavourDeviceAllocations(flavour.getComputeId()).stream()
                .map(cloudDeviceAllocation -> {
                    final CloudDevice cloudDevice = cloudDeviceAllocation.getDevice();
                    return allDevicePools.stream()
                        .filter(devicePool -> devicePool.getComputeIdentifier().equals(cloudDevice.getIdentifier()) && devicePool.getDeviceType().equals(fromCloudDeviceType(cloudDevice.getType())))
                        .findFirst()
                        .map(devicePool -> new FlavourDevice(flavour, devicePool, cloudDeviceAllocation.getUnitCount()))
                        .orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();

            final List<FlavourDevice> flavourDevices = flavour.getDevices();

            // Remove deleted devices
            flavourDevices.removeIf(flavourDevice -> !requiredDevices.contains(flavourDevice));

            // Add new devices
            for (final FlavourDevice requiredFlavourDevice : requiredDevices) {
                if (!flavourDevices.contains(requiredFlavourDevice)) {
                    flavourDevices.add(requiredFlavourDevice);
                }
            }

            this.save(flavour);

        } catch (Exception e) {
            logger.warn("Failed to get flavour devices for flavour with id: {}", flavour.getId());
        }
    }

}
