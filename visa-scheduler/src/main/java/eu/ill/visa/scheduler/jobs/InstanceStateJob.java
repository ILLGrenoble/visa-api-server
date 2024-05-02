package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.business.services.InstanceCommandService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceCommand;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


@ApplicationScoped
public class InstanceStateJob {

    private static final Logger logger = LoggerFactory.getLogger(InstanceStateJob.class);

    private final InstanceService instanceService;
    private final InstanceCommandService instanceCommandService;

    @Inject
    public InstanceStateJob(InstanceService instanceService, InstanceCommandService instanceCommandService) {
        this.instanceService = instanceService;
        this.instanceCommandService = instanceCommandService;
    }

    // Run every minute
    @Scheduled(cron="0 * * * * ?",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void updateAllInstancesStates() {
        List<Instance> instances = this.instanceService.getAll();
        logger.debug("Job running to update all instance states ({} instances)", instances.size());

        this.updateInstancesStates(instances);
    }

    // Run every 5 seconds
    @Scheduled(cron="0/5 * * * * ?",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void updateNonStableInstancesStates() {
        String[] statesString = {"BUILDING","STARTING","PARTIALLY_ACTIVE","REBOOTING","STOPPING","DELETED"};
        List<InstanceState> states = Arrays.stream(statesString).map(InstanceState::valueOf).toList();
        List<Instance> instances = this.instanceService.getAllWithStates(states);
        logger.debug("Job running to update instance states which have states {} ({} instances)", statesString, instances.size());

        this.updateInstancesStates(instances);
    }

    private void updateInstancesStates(final List<Instance> instances) {

        // Cleanup all instances that have deleted state - set soft deleted flag
        List<Instance> runningInstances = instances.stream()
            .filter(instance -> {
                if (instance.getState().equals(InstanceState.DELETED)) {
                    instance.setDeleted(true);
                    instanceService.save(instance);

                    return false;

                } else {
                    return true;
                }
            })
            .toList();

        // Update states of all running instances
        runningInstances.forEach(instance -> {
            InstanceCommand command = this.instanceCommandService.create(instance, InstanceCommandType.STATE);
            this.instanceCommandService.execute(command);
        });
    }
}
