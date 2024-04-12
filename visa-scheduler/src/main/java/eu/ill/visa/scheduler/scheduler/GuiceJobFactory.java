package eu.ill.visa.scheduler.scheduler;

import jakarta.inject.Injector;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GuiceJobFactory implements JobFactory {

    private final Injector guice;

    @Inject
    public GuiceJobFactory(final Injector guice) {
        this.guice = guice;
    }

    @Override
    public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler) {
        final JobDetail            jobDetail = bundle.getJobDetail();
        final Class<? extends Job> jobClass  = jobDetail.getJobClass();

        return guice.getInstance(jobClass);
    }
}
