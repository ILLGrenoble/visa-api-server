package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.business.services.HypervisorService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class HypervisorResourceUpdateJob {

    private static final Logger logger = LoggerFactory.getLogger(HypervisorResourceUpdateJob.class);

    private final HypervisorService hypervisorService;

    @Inject
    public HypervisorResourceUpdateJob(final HypervisorService hypervisorService) {
        this.hypervisorService = hypervisorService;
    }

    // Update inventory and usage every minute
    @Scheduled(cron="30 0/1 * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public synchronized void updateUsage() {
        this.hypervisorService.updateHypervisorInventories();
        this.hypervisorService.updateHypervisorUsages();
        this.hypervisorService.updateHypervisorAllocations();
    }
}
