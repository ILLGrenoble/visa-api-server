package eu.ill.visa.scheduler.jobs;

import com.google.inject.Inject;
import eu.ill.visa.business.services.InstanceCommandService;
import eu.ill.visa.core.domain.InstanceCommand;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@DisallowConcurrentExecution
public class InstanceCommandExecutorJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(InstanceCommandExecutorJob.class);

    private InstanceCommandService instanceCommandService;

    @Inject
    public InstanceCommandExecutorJob(InstanceCommandService instanceCommandService) {
        this.instanceCommandService = instanceCommandService;
    }

    @Override
    public synchronized void execute(JobExecutionContext context) {
        List<InstanceCommand> pendingCommands = this.instanceCommandService.getAllPending();
        if (pendingCommands.size() > 0) {
            logger.info("Running instance command executor job: executing {} pending commands", pendingCommands.size());
        }
        pendingCommands.forEach(instanceCommand -> this.instanceCommandService.execute(instanceCommand));
    }
}
