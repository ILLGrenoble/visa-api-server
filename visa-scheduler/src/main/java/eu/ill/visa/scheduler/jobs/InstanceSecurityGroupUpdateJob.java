package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.core.domain.fetches.InstanceFetch;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstanceCommandService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.SecurityGroupService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceCommand;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class InstanceSecurityGroupUpdateJob {

    private static final Logger logger = LoggerFactory.getLogger(InstanceSecurityGroupUpdateJob.class);

    private final InstanceService instanceService;
    private final SecurityGroupService securityGroupService;
    private final InstanceCommandService instanceCommandService;

    @Inject
    public InstanceSecurityGroupUpdateJob(final InstanceService instanceService,
                                          final SecurityGroupService securityGroupService,
                                          final InstanceCommandService instanceCommandService) {
        this.instanceService = instanceService;
        this.securityGroupService = securityGroupService;
        this.instanceCommandService = instanceCommandService;
    }

    // Run every day at 6am
    @Scheduled(cron="0 0 6 ? * *",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void execute() {
        logger.info("Executing instance security group update job");

        List<Instance> instances = this.instanceService.getAll(List.of(InstanceFetch.members, InstanceFetch.experiments, InstanceFetch.attributes));

        // Determine which instances have changed security groups
        for (Instance instance : instances) {
            List<String> currentSecurityGroups = instance.getSecurityGroups();
            List<String> updatedSecurityGroups = this.securityGroupService.getAllSecurityGroupNamesForInstance(instance);

            // Check if lists of security groups has changed
            if (!equalSecurityGroups(currentSecurityGroups, updatedSecurityGroups)) {
                // Create action to update the security groups of the instance
                logger.info("Updating security groups of instance {}", instance.getId());

                InstanceCommand command = this.instanceCommandService.create(instance, InstanceCommandType.UPDATE_SECURITY_GROUPS);
                this.instanceCommandService.execute(command);
            }
        }
    }

    private boolean equalSecurityGroups(List<String> list1, List<String> list2) {
        // Check for sizes and nulls
        if (list1 == null && list2 == null) {
            return true;
        }

        if (list1 == null || list2 == null || list1.size() != list2.size())  {
            return false;
        }

        // Sort and compare the two lists
        Collections.sort(list1);
        Collections.sort(list2);
        return list1.equals(list2);
    }
}
