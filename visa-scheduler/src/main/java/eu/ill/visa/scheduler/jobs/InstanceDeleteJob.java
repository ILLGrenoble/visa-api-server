package eu.ill.visa.scheduler.jobs;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstanceCommandService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceCommand;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class InstanceDeleteJob {

    private static final Logger logger = LoggerFactory.getLogger(InstanceDeleteJob.class);
    private final InstanceService instanceService;
    private final InstanceCommandService instanceCommandService;

    @Inject
    public InstanceDeleteJob(final InstanceService instanceService, final InstanceCommandService instanceCommandService) {
        this.instanceService = instanceService;
        this.instanceCommandService = instanceCommandService;
    }

    // Run every 10 seconds
    @Scheduled(cron="0/10 * * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void execute() {
        logger.debug("Executing instance delete job");

        List<Instance> instances = this.instanceService.getAllToDelete();
        instances.forEach(instance -> {
            instance.setState(InstanceState.DELETING);
            this.instanceService.save(instance);

            InstanceCommand command = instanceCommandService.create(instance.getOwner().getUser(), instance, InstanceCommandType.DELETE);
            instanceCommandService.execute(command);
        });

        if (instances.size() > 0) {
            logger.info("Instance delete job: deleted {} instances that were shutdown after a deletion request", instances.size());
        }
    }
}
