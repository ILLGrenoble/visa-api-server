package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.business.services.HypervisorService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


@ApplicationScoped
public class HypervisorResourceUpdateJob {

    private static final Logger logger = LoggerFactory.getLogger(HypervisorResourceUpdateJob.class);

    private final HypervisorService hypervisorService;
    private final InstanceService instanceService;

    @Inject
    public HypervisorResourceUpdateJob(final HypervisorService hypervisorService,
                                       final InstanceService instanceService) {
        this.hypervisorService = hypervisorService;
        this.instanceService = instanceService;
    }

    // Update inventory and usage every 10 seconds
    @Scheduled(cron="0/10 * * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public synchronized void updateMigrationUsage() {
        List<Instance> migratingInstances = this.instanceService.getAllWithStates(Arrays.asList(InstanceState.MIGRATING, InstanceState.ACTIVE_MIGRATING));
        if (!migratingInstances.isEmpty()) {
            this.hypervisorService.updateHypervisorData();
        }
    }

    // Update inventory and usage every minute
    @Scheduled(cron="30 * * ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public synchronized void updateUsage() {
        List<Instance> migratingInstances = this.instanceService.getAllWithStates(Arrays.asList(InstanceState.MIGRATING, InstanceState.ACTIVE_MIGRATING));
        if (migratingInstances.isEmpty()) {
            this.hypervisorService.updateHypervisorData();
        }
    }
}
