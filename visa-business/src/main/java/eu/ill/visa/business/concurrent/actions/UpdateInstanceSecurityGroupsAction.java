package eu.ill.visa.business.concurrent.actions;

import eu.ill.visa.business.concurrent.actions.exceptions.InstanceActionException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.SecurityGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class UpdateInstanceSecurityGroupsAction extends InstanceAction {

    private final static Logger logger = LoggerFactory.getLogger(UpdateInstanceSecurityGroupsAction.class);

    public UpdateInstanceSecurityGroupsAction(InstanceActionServiceProvider serviceProvider, InstanceCommand command) {
        super(serviceProvider, command);
    }

    @Override
    public void run() throws InstanceActionException {
        try {
            final CloudClient cloudClient = this.getCloudClient();

            final Instance instance = getInstance();

            // Test case when a instance has not yet been created on open stack
            if (instance == null || instance.getComputeId() == null) {
                return;
            }

            // Get security groups for instance
            List<SecurityGroup> securityGroups = this.getSecurityGroupService().getAllForInstance(instance);

            List<String> securityGroupNames = securityGroups.stream().map(SecurityGroup::getName).collect(Collectors.toUnmodifiableList());
            logger.info("Setting security groups [{}] to instance {}", String.join(", ", securityGroupNames), instance.getId());

            instance.setSecurityGroups(securityGroupNames);
            this.getInstanceService().save(instance);

            cloudClient.updateSecurityGroups(instance.getComputeId(), securityGroupNames);

        } catch (Exception exception) {
            logger.error("Error updating instance security groups for instance {} : {}", getInstance().getId(), exception.getMessage());
            throw new InstanceActionException("Error updating instance security groups", exception);
        }
    }
}
