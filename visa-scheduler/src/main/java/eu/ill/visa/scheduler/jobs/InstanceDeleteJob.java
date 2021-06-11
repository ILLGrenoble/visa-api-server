package eu.ill.visa.scheduler.jobs;

import com.google.inject.Inject;
import eu.ill.visa.business.services.InstanceCommandService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@DisallowConcurrentExecution
public class InstanceDeleteJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(InstanceDeleteJob.class);
    private final InstanceService instanceService;
    private final InstanceCommandService instanceCommandService;

    @Inject
    public InstanceDeleteJob(final InstanceService instanceService, final InstanceCommandService instanceCommandService) {
        this.instanceService = instanceService;
        this.instanceCommandService = instanceCommandService;
    }

    @Override
    public void execute(JobExecutionContext context) {
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
