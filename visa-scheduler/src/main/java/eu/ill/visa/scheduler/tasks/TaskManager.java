package eu.ill.visa.scheduler.tasks;

import com.google.inject.Inject;
import jakarta.inject.Singleton;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Singleton
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
