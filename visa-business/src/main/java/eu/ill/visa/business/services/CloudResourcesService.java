package eu.ill.visa.business.services;

import eu.ill.visa.cloud.domain.CloudLimit;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.core.entity.CloudResources;
import eu.ill.visa.persistence.repositories.CloudResourcesRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Transactional
@Singleton
public class CloudResourcesService {
    private static final Logger logger = LoggerFactory.getLogger(CloudResourcesService.class);

    private final CloudResourcesRepository repository;
    private final CloudClientService cloudClientService;
    private final CloudProviderService cloudProviderService;

    @Inject
    public CloudResourcesService(final CloudResourcesRepository repository,
                                 final CloudClientService cloudClientService,
                                 final CloudProviderService cloudProviderService) {
        this.repository = repository;
        this.cloudClientService = cloudClientService;
        this.cloudProviderService = cloudProviderService;
    }

    public List<CloudResources> getAll() {
        return this.repository.getAll();
    }

    public void save(@NotNull CloudResources cloudResources) {
        this.repository.save(cloudResources);
    }

    public void delete(@NotNull CloudResources cloudResources) {
        this.repository.delete(cloudResources);
    }

    public void updateCloudResources() {

        List<CloudClient> cloudClients = this.cloudClientService.getAll();
        final List<CloudResources> allResources = this.getAll();

        // Delete non existent
        allResources.stream().filter(cloudResources -> {
            return cloudClients.stream()
                .filter(cloudClient -> {
                    long resourceCloudId = cloudResources.getCloudId() == null ? -1 : cloudResources.getCloudId();
                    long cloudClientId = cloudClient.getId() == null ? -1 : cloudClient.getId();
                    return cloudClientId == resourceCloudId;
                })
                .findFirst()
                .isEmpty();
        }).forEach(this::delete);

        // Update or create
        cloudClients.forEach(cloudClient -> {
            try {
                CloudLimit cloudLimit = cloudClient.getProvider().limits();

                allResources.stream().filter(resources -> {
                    long resourceCloudId = resources.getCloudId() == null ? -1 : resources.getCloudId();
                    long cloudClientId = cloudClient.getId() == null ? -1 : cloudClient.getId();
                    return resourceCloudId == cloudClientId;
                }).findFirst().ifPresentOrElse(
                    resources -> {
                        resources.setInstancesTotal((long)cloudLimit.getMaxTotalInstances());
                        resources.setInstancesUsage((long)cloudLimit.getTotalInstancesUsed());
                        resources.setVcpuTotal((long)cloudLimit.getMaxTotalCores());
                        resources.setVcpuUsage((long)cloudLimit.getTotalCoresUsed());
                        resources.setMemoryMbTotal((long)cloudLimit.getMaxTotalRAMSize());
                        resources.setMemoryMbUsage((long)cloudLimit.getTotalRAMUsed());
                        this.save(resources);
                    },
                    () -> {
                        CloudProviderConfiguration cloudProviderConfiguration = this.cloudProviderService.getById(cloudClient.getId());

                        CloudResources cloudResources = CloudResources.Builder()
                            .instancesTotal((long)cloudLimit.getMaxTotalInstances())
                            .instancesUsage((long)cloudLimit.getTotalInstancesUsed())
                            .vcpuTotal((long)cloudLimit.getMaxTotalCores())
                            .vcpuUsage((long)cloudLimit.getTotalCoresUsed())
                            .memoryMbTotal((long)cloudLimit.getMaxTotalRAMSize())
                            .memoryMbUsage((long)cloudLimit.getTotalRAMUsed())
                            .cloudProviderConfiguration(cloudProviderConfiguration)
                            .build();
                        this.save(cloudResources);
                    });

            } catch (CloudException e) {
                logger.warn("Failed to retrieve cloud limits from CloudClient: {}", e.getMessage());
            }
        });
    }

}
