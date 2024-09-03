package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.cloud.domain.CloudInstanceIdentifier;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.Instance;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CloudInstanceCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(CloudInstanceCleanupJob.class);

    private final InstanceService instanceService;

    @Inject
    public CloudInstanceCleanupJob(final InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    // Run every day at 2am
    @Scheduled(cron="0 0 2 ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void execute() {
        logger.info("Executing cloud instance cleanup job");

        Map<CloudClient, List<Instance>> instancesByCloud = this.instanceService.getAllByCloud();
        for (Map.Entry<CloudClient, List<Instance>> entry : instancesByCloud.entrySet()) {
            CloudClient cloudClient = entry.getKey();
            List<Instance> instances = entry.getValue();

            try {
                List<CloudInstanceIdentifier> cloudInstances = cloudClient.instanceIdentifiers();

                String serverNamePrefix = cloudClient.getServerNamePrefix();

                List<CloudInstanceIdentifier> zombieCloudInstances = cloudInstances.stream().filter(cloudInstance -> {
                    boolean instanceNotExists = instances.stream().noneMatch(instance -> cloudInstance.getId().equals(instance.getComputeId()));
                    return instanceNotExists && cloudInstance.getName().startsWith(serverNamePrefix);
                }).toList();

                logger.info("Found {} cloud instances, on Cloud \"{}\", that no longer exist in the database", zombieCloudInstances.size(), cloudClient.getName());
                for (CloudInstanceIdentifier cloudInstance : zombieCloudInstances) {
                    logger.info("Deleting cloud instance {} with id {} on Cloud \"{}\"", cloudInstance.getName(), cloudInstance.getId(), cloudClient.getName());
                    cloudClient.deleteInstance(cloudInstance.getId());
                }

            } catch (CloudException e) {
                logger.error("Failed to get cloud instances on cloud \"{}\" during cleanup job: {}", cloudClient.getName(), e.getMessage());
            }
        }


    }
}
