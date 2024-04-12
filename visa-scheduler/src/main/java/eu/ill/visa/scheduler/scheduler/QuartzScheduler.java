package eu.ill.visa.scheduler.scheduler;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;

import com.google.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class QuartzScheduler {

    private final Scheduler scheduler;

    @Inject
    public QuartzScheduler(final SchedulerFactory factory, final GuiceJobFactory jobFactory) throws SchedulerException {
        this.scheduler = factory.getScheduler();
        this.scheduler.setJobFactory(jobFactory);
    }


    public final Scheduler getScheduler() {
        return this.scheduler;
    }
}
