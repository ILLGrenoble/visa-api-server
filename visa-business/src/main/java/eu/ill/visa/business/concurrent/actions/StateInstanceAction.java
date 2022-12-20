package eu.ill.visa.business.concurrent.actions;

import eu.ill.visa.business.concurrent.actions.exceptions.InstanceActionException;
import eu.ill.visa.business.services.PortService;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.domain.CloudInstanceState;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

            CloudInstance cloudInstance = cloudClient.instance(instance.getComputeId());

            InstanceState instanceState;
            if (cloudInstance == null) {
                logger.warn("Instance {} has been deleted on the Open Stack server", instance.getComputeId());
                instanceState = InstanceState.DELETED;
                this.updateInstanceState(instanceState);
                this.updateInstanceIpAddress(null);

            } else {
                logger.debug("Fetched cloud instance with address :{}", cloudInstance.getAddress());
                CloudInstanceState cloudInstanceState = cloudInstance.getState();
                if (cloudInstanceState.equals(CloudInstanceState.ACTIVE)) {
                    this.updateInstanceIpAddress(cloudInstance.getAddress());
                }
                instanceState = InstanceState.valueOf(cloudInstanceState.toString());

                // Check if we have requested it to be deleted and provider says it is active, or requested to restart and provider says it is still stopped
                boolean ignoreCloudState = (instance.getState().equals(InstanceState.DELETING) && cloudInstanceState.equals(CloudInstanceState.ACTIVE)) ||
                    (instance.getState().equals(InstanceState.STARTING) && cloudInstanceState.equals(CloudInstanceState.STOPPED));

                // Update instance state in the database (unless we have requested it to be deleted and provider says it is active)
                if (!ignoreCloudState) {
                    if (instanceState.equals(InstanceState.ACTIVE) || instanceState.equals(InstanceState.PARTIALLY_ACTIVE)) {
                        logger.debug("Checking ports are open for address: {}", cloudInstance.getAddress());
                        final Plan plan = instance.getPlan();
                        final Image image = plan.getImage();
                        final List<ImageProtocol> protocols = image.getProtocols();
                        boolean instanceIsUpAndRunning = PortService.areMandatoryPortsOpen(cloudInstance.getAddress(), protocols);
                        if (!instanceIsUpAndRunning) {
                            instanceState = InstanceState.STARTING;

                        } else {
                            List<ImageProtocol> activeProtocols = PortService.getActiveProtocols(cloudInstance.getAddress(), protocols);
                            if (activeProtocols.size() < protocols.size()) {
                                instanceState = InstanceState.PARTIALLY_ACTIVE;
                            } else {

                            }
                            this.updateInstanceProtocols(activeProtocols);
                        }
                    }
                    this.updateInstanceState(instanceState);
                }
            }

        } catch (Exception exception) {
            logger.error("Error getting state of a compute instance {}: {}", getCommand().getInstance().getId(), exception.getMessage());
            throw new InstanceActionException("Error getting state of a compute instance", exception);
        }

    }
}
