package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.business.services.DesktopSessionRttSampleService;
import eu.ill.visa.business.services.InstanceService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class InstanceSessionSampleReducerJob {

    private static final Logger logger = LoggerFactory.getLogger(InstanceSessionSampleReducerJob.class);

    private final DesktopSessionRttSampleService desktopSessionRttSampleService;
    private final InstanceService instanceService;

    @Inject
    public InstanceSessionSampleReducerJob(final DesktopSessionRttSampleService desktopSessionRttSampleService,
                                           final InstanceService instanceService) {
        this.desktopSessionRttSampleService = desktopSessionRttSampleService;
        this.instanceService = instanceService;
    }

    // Run every hour
    @Scheduled(cron="0 5 * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void execute() {
        logger.info("Executing instance session sample reducer job...");

        Integer retentionPeriod = this.instanceService.getRTTSampleRetentionPeriodDays();
        Pair<Integer, Integer> reduction = this.desktopSessionRttSampleService.performResampling(retentionPeriod);

        logger.info("... reduced {} samples to {} hourly ones", reduction.getLeft(), reduction.getRight());
    }
}
