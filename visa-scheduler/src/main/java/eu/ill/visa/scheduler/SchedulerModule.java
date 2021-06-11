package eu.ill.visa.scheduler;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.ill.visa.scheduler.jobs.InstanceAuthenticationTokenDeleteJob;
import eu.ill.visa.scheduler.jobs.InstanceStateJob;
import eu.ill.visa.scheduler.scheduler.GuiceJobFactory;
import eu.ill.visa.scheduler.scheduler.QuartzScheduler;
import eu.ill.visa.scheduler.tasks.TaskManager;
import eu.ill.visa.scheduler.tasks.TaskManagerConfiguration;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerModule extends AbstractModule {

    private final EventBus eventBus = new EventBus("Default EventBus");

    @Override
    protected void configure() {
        bindEvents();
        bindJobs();
        bindScheduler();
        bindTaskManager();
    }

    private void bindTaskManager() {
        bind(TaskManager.class);
    }

    private void bindEvents() {
        bind(EventBus.class).toInstance(eventBus);
    }

    private void bindJobs() {
        bind(InstanceStateJob.class);
        bind(InstanceAuthenticationTokenDeleteJob.class);
    }

    private void bindScheduler() {
        bind(SchedulerFactory.class).toInstance(new StdSchedulerFactory());
        bind(GuiceJobFactory.class);
        bind(QuartzScheduler.class).asEagerSingleton();
    }

    @Provides
    public TaskManagerConfiguration providesTaskManagerConfiguration(
        final SchedulerConfiguration configuration) {
        return configuration.getTaskManagerConfiguration();
    }

}
