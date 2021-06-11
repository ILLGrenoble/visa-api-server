package eu.ill.visa.web.managed;

import com.google.inject.Inject;
import eu.ill.visa.scheduler.SchedulerConfiguration;
import eu.ill.visa.scheduler.scheduler.QuartzScheduler;
import io.dropwizard.lifecycle.Managed;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedScheduler implements Managed {

    private final static Logger logger = LoggerFactory.getLogger(ManagedScheduler.class);
    private final Scheduler scheduler;
    private SchedulerConfiguration configuration;

    @Inject
    public ManagedScheduler(final QuartzScheduler scheduler, final SchedulerConfiguration configuration) {
        this.scheduler = scheduler.getScheduler();
        this.configuration = configuration;
    }

    @Override
    public void start() throws Exception {
        try {
            if (configuration.isEnabled()) {
                logger.info("Starting scheduler");
                this.scheduler.start();
            } else {
                logger.info("Scheduler is disabled");
            }

        } catch (Exception e) {
            logger.error("Error creating Virtual Desktop Application: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        if (configuration.isEnabled()) {
            logger.info("Stopping scheduler");
            this.scheduler.shutdown();
        }
    }
}
