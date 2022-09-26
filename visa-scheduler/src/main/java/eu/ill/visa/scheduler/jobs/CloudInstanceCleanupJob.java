package eu.ill.visa.scheduler.jobs;

import com.google.inject.Inject;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.cloud.domain.CloudInstanceIdentifier;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.domain.Instance;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@DisallowConcurrentExecution
public class CloudInstanceCleanupJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(CloudInstanceCleanupJob.class);

    private final InstanceService instanceService;
    private final CloudClientGateway cloudClientGateway;

    @Inject
    public CloudInstanceCleanupJob(final InstanceService instanceService, final CloudClientGateway cloudClientGateway) {
        this.instanceService = instanceService;
        this.cloudClientGateway = cloudClientGateway;
    }

    @Override
    public void execute(final JobExecutionContext context) {
        logger.info("Executing cloud instance cleanup job");

        try {
            List<Instance> instances = this.instanceService.getAll();
            // TODO CloudClient: select specific cloud client
            CloudClient cloudClient = this.cloudClientGateway.getDefaultCloudClient();

            List<CloudInstanceIdentifier> cloudInstances = cloudClient.instanceIdentifiers();

            String serverNamePrefix = cloudClient.getServerNamePrefix();

            List<CloudInstanceIdentifier> zombieCloudInstances = cloudInstances.stream().filter(cloudInstance -> {
                boolean instanceNotExists = instances.stream().noneMatch(instance -> cloudInstance.getId().equals(instance.getComputeId()));
                return instanceNotExists && cloudInstance.getName().startsWith(serverNamePrefix);
            }).collect(Collectors.toUnmodifiableList());

            logger.info("Found {} cloud instances that no longer exist in the database", zombieCloudInstances.size());
            for (CloudInstanceIdentifier cloudInstance : zombieCloudInstances) {
                logger.info("Deleting cloud instance {} with id {}", cloudInstance.getName(), cloudInstance.getId());
                cloudClient.deleteInstance(cloudInstance.getId());
            }

        } catch (CloudException e) {
            logger.error("Failed to get cloud instances during cleanup job: {}", e.getMessage());
        }
    }
}
