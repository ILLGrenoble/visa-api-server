package eu.ill.visa.business.concurrent.actions;

import eu.ill.visa.business.concurrent.actions.exceptions.InstanceActionException;
import eu.ill.visa.business.gateway.AdminEvent;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.domain.CloudInstanceState;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceCommand;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateInstanceAction extends InstanceAction {

    private final static Logger logger = LoggerFactory.getLogger(StateInstanceAction.class);

    public StateInstanceAction(InstanceActionServiceProvider serviceProvider, InstanceCommand command) {
        super(serviceProvider, command);
    }

    @Override
    public void run() throws InstanceActionException {
        try {
            final Instance instance = getInstance();

            // Test case when a instance has not yet been created on open stack
            if (instance == null || instance.getComputeId() == null) {
                return;
            }

            final CloudClient cloudClient = this.getCloudClient(instance.getCloudId());
            if (cloudClient == null) {
                return;
            }

            InstanceState oldState = instance.getState();

            CloudInstance cloudInstance = cloudClient.instance(instance.getComputeId());

            InstanceState instanceState;
            if (cloudInstance == null) {
                logger.warn("Instance {} has been deleted on the cloud provider", instance.getComputeId());
                instanceState = InstanceState.DELETED;
                this.updateInstanceState(instanceState);
                this.updateInstanceIpAddress(null);

            } else {
                logger.trace("Fetched cloud instance with address :{}", cloudInstance.getAddress());
                CloudInstanceState cloudInstanceState = cloudInstance.getState();
                if (cloudInstanceState.equals(CloudInstanceState.ACTIVE)) {
                    this.updateInstanceIpAddress(cloudInstance.getAddress());
                }
                instanceState = InstanceState.valueOf(cloudInstanceState.toString());

                // Check if we have requested it to be deleted and provider says it is active, or requested to restart and provider says it is still stopped
                InstanceCommand lastUserCommand = this.getInstanceCommandService().getLastUserCommandForInstance(instance);
                boolean ignoreCloudState = (lastUserCommand.getActionType().equals(InstanceCommandType.DELETE) && cloudInstanceState.equals(CloudInstanceState.ACTIVE)) ||
                    (lastUserCommand.getActionType().equals(InstanceCommandType.REBOOT) && cloudInstanceState.equals(CloudInstanceState.STOPPED));

                // Update instance state in the database (unless we have requested it to be deleted and provider says it is active)
                if (!ignoreCloudState) {
                    if (instanceState.isActive()) {
                        instanceState = this.verifyActiveInstance(instance, cloudInstance.getAddress());
                    }
                    this.updateInstanceState(instanceState);
                }
            }

            // Check to see if instance state has changed from or to an error
            if ((oldState.equals(InstanceState.ERROR) && !instanceState.equals(InstanceState.ERROR)) || (!oldState.equals(InstanceState.ERROR) && instanceState.equals(InstanceState.ERROR))) {
                this.getEventDispatcher().sendEventForRole(Role.ADMIN_ROLE, AdminEvent.INSTANCE_ERRORS_CHANGED);
            }

        } catch (Exception exception) {
            logger.error("Error getting state of a compute instance {}: {}", getCommand().getInstance().getId(), exception.getMessage());
            throw new InstanceActionException("Error getting state of a compute instance", exception);
        }

    }
}
