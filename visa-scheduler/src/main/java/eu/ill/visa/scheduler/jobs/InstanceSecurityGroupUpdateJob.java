package eu.ill.visa.scheduler.jobs;

import com.google.inject.Inject;
import eu.ill.visa.business.services.InstanceCommandService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.SecurityGroupService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.SecurityGroup;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@DisallowConcurrentExecution
public class InstanceSecurityGroupUpdateJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(InstanceSecurityGroupUpdateJob.class);

    private InstanceService instanceService;
    private SecurityGroupService securityGroupService;
    private InstanceCommandService instanceCommandService;

    @Inject
    public InstanceSecurityGroupUpdateJob(InstanceService instanceService, SecurityGroupService securityGroupService, InstanceCommandService instanceCommandService) {
        this.instanceService = instanceService;
        this.securityGroupService = securityGroupService;
        this.instanceCommandService = instanceCommandService;
    }

    @Override
    public void execute(final JobExecutionContext context) {
        logger.info("Executing instance security group update job");

        List<Instance> instances = this.instanceService.getAll();

        // Determine which instances have changed security groups
        for (Instance instance : instances) {
            List<String> currentSecurityGroups = instance.getSecurityGroups();
            List<String> updatedSecurityGroups = this.securityGroupService.getAllForInstance(instance).stream()
                .map(SecurityGroup::getName).collect(Collectors.toList());

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

        if ((list1 == null && list2 != null) || (list1 != null && list2 == null) || (list1.size() != list2.size()))  {
            return false;
        }

        // Sort and compare the two lists
        Collections.sort(list1);
        Collections.sort(list2);
        return list1.equals(list2);
    }
}
