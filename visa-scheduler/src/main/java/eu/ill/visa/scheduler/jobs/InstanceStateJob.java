package eu.ill.visa.scheduler.jobs;

import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstanceCommandService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@DisallowConcurrentExecution
public class InstanceStateJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(InstanceStateJob.class);

    private InstanceService instanceService;
    private InstanceCommandService instanceCommandService;

    @Inject
    public InstanceStateJob(InstanceService instanceService, InstanceCommandService instanceCommandService) {
        this.instanceService = instanceService;
        this.instanceCommandService = instanceCommandService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        String statesString = context.getMergedJobDataMap().getString("states");

        List<Instance> instances;
        if (statesString == null || statesString.length() == 0) {
            instances = this.instanceService.getAll();
            logger.debug("Job running to update all instance states ({} instances)", instances.size());

        } else {
            List<InstanceState> states = Arrays.asList(statesString.split(",")).stream().map(stateString -> InstanceState.valueOf(stateString)).collect(Collectors.toList());
            instances = this.instanceService.getAllWithStates(states);
            logger.debug("Job running to update instance states which have states {} ({} instances)", statesString, instances.size());
        }

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
            .collect(Collectors.toList());

        // Update states of all running instances
        runningInstances.forEach(instance -> {
            InstanceCommand command = this.instanceCommandService.create(instance, InstanceCommandType.STATE);
            this.instanceCommandService.execute(command);
        });
    }
}
