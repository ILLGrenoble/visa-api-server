package eu.ill.visa.business.services;

import eu.ill.visa.cloud.domain.CloudHypervisorAllocation;
import eu.ill.visa.cloud.domain.CloudHypervisorInventory;
import eu.ill.visa.cloud.domain.CloudHypervisorUsage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudUnavailableException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.core.entity.Hypervisor;
import eu.ill.visa.core.entity.HypervisorAllocation;
import eu.ill.visa.core.entity.HypervisorResource;
import eu.ill.visa.persistence.repositories.HypervisorRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Singleton
public class HypervisorService {
    private static final Logger logger = LoggerFactory.getLogger(HypervisorService.class);

    private final HypervisorRepository repository;
    private final CloudClientService cloudClientService;
    private final CloudProviderService cloudProviderService;

    @Inject
    public HypervisorService(final HypervisorRepository repository,
                             final CloudClientService cloudClientService,
                             final CloudProviderService cloudProviderService) {
        this.repository = repository;
        this.cloudClientService = cloudClientService;
        this.cloudProviderService = cloudProviderService;
    }

    public List<Hypervisor> getAll() {
        return this.repository.getAll();
    }

    public Long countAll() {
        return this.repository.countAll();
    }

    public List<Hypervisor> getAllAvailable() {
        return this.repository.getAllAvailable();
    }

    public List<Resource> getTotalResources() {
        return this.getAll().stream()
            .flatMap(hypervisor -> hypervisor.getResources().stream())
            .collect(Collectors.toMap(
                HypervisorResource::getResourceClass,
                r -> new Resource(r.getResourceClass(), r.getTotal(), r.getUsage()),
                (r1, r2) -> new Resource(r1.getResourceClass(), r1.getTotal() + r2.getTotal(), r1.getUsage() + r2.getUsage())
            ))
            .values()
            .stream()
            .toList();
    }

    public void save(@NotNull Hypervisor hypervisor) {
        this.repository.save(hypervisor);
    }

    public void delete(@NotNull Hypervisor hypervisor) {
        this.repository.delete(hypervisor);
    }

    public synchronized void updateHypervisorInventories() {
        this.cloudClientService.getAll().forEach(this::updateCloudClientHypervisorInventories);
    }

    public synchronized void updateHypervisorUsages() {
        this.cloudClientService.getAll().forEach(this::updateCloudClientHypervisorUsages);
    }

    public void updateHypervisorAllocations() {
        this.cloudClientService.getAll().forEach(this::updateCloudClientHypervisorAllocations);
    }

    private synchronized void updateCloudClientHypervisorInventories(final CloudClient cloudClient) {
        CloudProviderConfiguration cloudProviderConfiguration = this.cloudProviderService.getById(cloudClient.getId());

        try {
            final List<CloudHypervisorInventory> hypervisorInventories = cloudClient.getProvider().hypervisorInventories();

            final List<Hypervisor> currentHypervisors = this.getAll();

            // Find removed hypervisors
            currentHypervisors.stream()
                .filter(hypervisor -> {
                    long hypervisorCloudId = hypervisor.getCloudId() == null ? -1 : hypervisor.getCloudId();
                    long cloudClientId = cloudClient.getId() == null ? -1 : cloudClient.getId();
                    return hypervisorCloudId == cloudClientId;
                })
                .filter(hypervisor -> hypervisorInventories.stream()
                    .filter(hypervisorInventory -> hypervisorInventory.getHypervisor().getId().equals(hypervisor.getComputeId()))
                    .findFirst()
                    .isEmpty())
                .forEach(this::delete);

            // Update or create
            hypervisorInventories.forEach(hypervisorInventory -> {
                currentHypervisors.stream()
                    .filter(hypervisor -> hypervisor.getComputeId().equals(hypervisorInventory.getHypervisor().getId()))
                    .findFirst()
                    .ifPresentOrElse(hypervisor -> {
                        hypervisor.setHostname(hypervisorInventory.getHypervisor().getHostname());
                        hypervisor.setStatus(hypervisorInventory.getHypervisor().getStatus());
                        hypervisor.setState(hypervisorInventory.getHypervisor().getState());
                        hypervisor.updateResourceInventory(hypervisorInventory.getInventory());
                        this.save(hypervisor);
                    }, () -> {
                        Hypervisor newHypervisor = Hypervisor.Builder()
                            .computeId(hypervisorInventory.getHypervisor().getId())
                            .hostname(hypervisorInventory.getHypervisor().getHostname())
                            .status(hypervisorInventory.getHypervisor().getStatus())
                            .state(hypervisorInventory.getHypervisor().getState())
                            .resourceInventory(hypervisorInventory.getInventory())
                            .cloudProviderConfiguration(cloudProviderConfiguration)
                            .build();
                        this.save(newHypervisor);
                    });
            });

        } catch (CloudException e) {
            logger.warn("Failed to retrieve hypervisor inventories from CloudClient: {}", e.getMessage());

        } catch (CloudUnavailableException e) {
            // Ignore
        }
    }

    private synchronized void updateCloudClientHypervisorUsages(final CloudClient cloudClient) {
        try {
            final List<CloudHypervisorUsage> hypervisorUsages = cloudClient.getProvider().hypervisorUsages();

            final List<Hypervisor> hypervisors = this.getAll();

            // Update usages
            hypervisorUsages.forEach(hypervisorUsage -> {
                hypervisors.stream()
                    .filter(hypervisor -> {
                        long hypervisorCloudId = hypervisor.getCloudId() == null ? -1 : hypervisor.getCloudId();
                        long cloudClientId = cloudClient.getId() == null ? -1 : cloudClient.getId();
                        return  hypervisorCloudId == cloudClientId;
                    })
                    .filter(hypervisor -> hypervisor.getComputeId().equals(hypervisorUsage.getHypervisor().getId()))
                    .findFirst()
                    .ifPresent(hypervisor -> {
                        hypervisor.updateResourceUsage(hypervisorUsage.getUsage());
                        this.save(hypervisor);
                    });
            });

        } catch (CloudException e) {
            logger.warn("Failed to retrieve hypervisor usages from CloudClient: {}", e.getMessage());

        } catch (CloudUnavailableException e) {
            // Ignore
        }
    }

    private synchronized void updateCloudClientHypervisorAllocations(final CloudClient cloudClient) {
        try {
            final List<CloudHypervisorAllocation> hypervisorAllocations = cloudClient.getProvider().hypervisorAllocations();

            final List<Hypervisor> hypervisors = this.getAll();

            // Update allocations
            hypervisorAllocations.forEach(hypervisorAllocation -> {
                hypervisors.stream()
                    .filter(hypervisor -> {
                        long hypervisorCloudId = hypervisor.getCloudId() == null ? -1 : hypervisor.getCloudId();
                        long cloudClientId = cloudClient.getId() == null ? -1 : cloudClient.getId();
                        return hypervisorCloudId == cloudClientId;
                    })
                    .filter(hypervisor -> hypervisor.getComputeId().equals(hypervisorAllocation.getHypervisor().getId()))
                    .findFirst()
                    .ifPresent(hypervisor -> {
                        hypervisor.updateAllocations(hypervisorAllocation.getAllocations().stream().map(cloudAllocation -> new HypervisorAllocation(cloudAllocation.getServerComputeId())).toList());
                        this.save(hypervisor);
                    });
            });

        } catch (CloudException e) {
            logger.warn("Failed to retrieve hypervisor allocations from CloudClient: {}", e.getMessage());

        } catch (CloudUnavailableException e) {
            // Ignore
        }

    }

    public void onCloudClientUpdated(final CloudClient cloudClient) {
        if (cloudClient != null) {
            // Run in thread to avoid latency with request
            Thread.startVirtualThread(() -> {
                this.updateCloudClientHypervisorInventories(cloudClient);
                this.updateCloudClientHypervisorUsages(cloudClient);
                this.updateCloudClientHypervisorAllocations(cloudClient);
            });
        }
    }

    public void onCloudClientDeleted(final CloudClient cloudClient) {
        final List<Hypervisor> currentHypervisors = this.getAll();

        // Find removed hypervisors
        currentHypervisors.stream()
            .filter(hypervisor -> {
                long hypervisorCloudId = hypervisor.getCloudId() == null ? -1 : hypervisor.getCloudId();
                long cloudClientId = cloudClient.getId() == null ? -1 : cloudClient.getId();
                return hypervisorCloudId == cloudClientId;
            })
            .forEach(this::delete);
    }

    public final static class Resource {
        private String resourceClass;
        private Long total;
        private Long usage;

        public Resource(String resourceClass, Long total, Long usage) {
            this.resourceClass = resourceClass;
            this.total = total;
            this.usage = usage;
        }

        public String getResourceClass() {
            return resourceClass;
        }

        public void setResourceClass(String resourceClass) {
            this.resourceClass = resourceClass;
        }

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public Long getUsage() {
            return usage;
        }

        public void setUsage(Long usage) {
            this.usage = usage;
        }
    }

}
