package eu.ill.visa.scheduler.tasks;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@ApplicationScoped
public class TaskManager {

    private final ThreadPoolExecutor threadPoolExecutor;

    @Inject
    public TaskManager(final TaskManagerConfiguration configuration) {
        this.threadPoolExecutor = new ScheduledThreadPoolExecutor(configuration.getNumberThreads());
    }

    public void submit(final Runnable task) {
        this.threadPoolExecutor.submit(task);
    }
}
