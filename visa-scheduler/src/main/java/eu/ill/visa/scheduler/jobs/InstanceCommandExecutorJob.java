package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.core.entity.InstanceCommand;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstanceCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@ApplicationScoped
public class InstanceCommandExecutorJob {

    private static final Logger logger = LoggerFactory.getLogger(InstanceCommandExecutorJob.class);

    private final InstanceCommandService instanceCommandService;

    @Inject
    public InstanceCommandExecutorJob(InstanceCommandService instanceCommandService) {
        this.instanceCommandService = instanceCommandService;
    }

    // Run every 2 seconds
    @Scheduled(cron="0/2 * * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public synchronized void execute() {
        List<InstanceCommand> pendingCommands = this.instanceCommandService.getAllPending();
        if (!pendingCommands.isEmpty()) {
            logger.info("Running instance command executor job: executing {} pending commands", pendingCommands.size());
        }
        pendingCommands.forEach(instanceCommand -> this.instanceCommandService.execute(instanceCommand));
    }
}
