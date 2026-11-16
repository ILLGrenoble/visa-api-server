package eu.ill.visa.business.scheduler;


import eu.ill.visa.business.SchedulerConfiguration;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SchedulerNotEnabled implements Scheduled.SkipPredicate {

    private final SchedulerConfiguration schedulerConfiguration;

    public SchedulerNotEnabled(final SchedulerConfiguration schedulerConfiguration) {
        this.schedulerConfiguration = schedulerConfiguration;
    }

    @Override
    public boolean test(ScheduledExecution execution) {
        return !this.schedulerConfiguration.enabled();
    }
}
