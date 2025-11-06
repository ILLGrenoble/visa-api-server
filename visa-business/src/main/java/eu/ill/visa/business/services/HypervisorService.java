package eu.ill.visa.business.services;

import eu.ill.visa.cloud.domain.CloudHypervisorInventory;
import eu.ill.visa.cloud.domain.CloudHypervisorUsage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudUnavailableException;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.core.entity.Hypervisor;
import eu.ill.visa.persistence.repositories.HypervisorRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    public void save(@NotNull Hypervisor hypervisor) {
        this.repository.save(hypervisor);
    }

    public void delete(@NotNull Hypervisor hypervisor) {
        this.repository.delete(hypervisor);
    }

    public void updateHypervisorInventories() {
        this.cloudClientService.getAll().forEach(cloudClient -> {
            CloudProviderConfiguration cloudProviderConfiguration = this.cloudProviderService.getById(cloudClient.getId());

            try {
                final List<CloudHypervisorInventory> hypervisorInventories = cloudClient.getProvider().hypervisorInventories();

                final List<Hypervisor> currentHypervisors = this.getAll();

                // Find removed hypervisors
                currentHypervisors.stream()
                    .filter(hypervisor -> hypervisorInventories.stream()
                        .filter(hypervisorInventory -> hypervisorInventory.getHypervisor().getId().equals(hypervisor.getCloudId()))
                        .findFirst()
                        .isEmpty())
                    .forEach(this::delete);

                // Update or create
                hypervisorInventories.forEach(hypervisorInventory -> {
                    currentHypervisors.stream()
                        .filter(hypervisor -> hypervisor.getCloudId().equals(hypervisorInventory.getHypervisor().getId()))
                        .findFirst()
                        .ifPresentOrElse(hypervisor -> {
                            hypervisor.setHostname(hypervisorInventory.getHypervisor().getHostname());
                            hypervisor.setStatus(hypervisorInventory.getHypervisor().getStatus());
                            hypervisor.setState(hypervisorInventory.getHypervisor().getState());
                            hypervisor.updateResourceInventory(hypervisorInventory.getInventory());
                            this.save(hypervisor);
                        }, () -> {
                            Hypervisor newHypervisor = Hypervisor.Builder()
                                .cloudId(hypervisorInventory.getHypervisor().getId())
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


        });
    }
    public void updateHypervisorUsages() {
        this.cloudClientService.getAll().forEach(cloudClient -> {
            CloudProviderConfiguration cloudProviderConfiguration = this.cloudProviderService.getById(cloudClient.getId());

            try {
                final List<CloudHypervisorUsage> hypervisorUsages = cloudClient.getProvider().hypervisorUsages();

                final List<Hypervisor> hypervisors = this.getAll();

                // Update or create
                hypervisorUsages.forEach(hypervisorUsage -> {
                    hypervisors.stream()
                        .filter(hypervisor -> hypervisor.getCloudId().equals(hypervisorUsage.getHypervisor().getId()))
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
        });
    }

}
