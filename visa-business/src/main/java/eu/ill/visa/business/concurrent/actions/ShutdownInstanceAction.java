package eu.ill.visa.business.concurrent.actions;

import eu.ill.visa.business.concurrent.actions.exceptions.InstanceActionException;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownInstanceAction extends InstanceAction {

    private final static Logger logger = LoggerFactory.getLogger(ShutdownInstanceAction.class);

    public ShutdownInstanceAction(InstanceActionServiceProvider serviceProvider, InstanceCommand command) {
        super(serviceProvider, command);
    }

    @Override
    public void run() throws InstanceActionException {
        try {
            final Instance instance = getInstance();
            if (instance == null) {
                return;
            }

            final CloudClient cloudClient = this.getCloudClient(instance.getCloudId());
            if (cloudClient == null) {
                return;
            }

            final String computeId = instance.getComputeId();
            if (computeId == null) {
                logger.info("Compute instance {} does not have a compute id associated to it. Ignoring shutdown action", instance.getId());
            } else {
                final CloudInstance cloudInstance = cloudClient.instance(computeId);
                if (cloudInstance == null) {
                    logger.info("Could not find compute instance: {}", computeId);
                } else {
                    logger.info("Shutting down compute instance: {}", instance.getId());
                    cloudClient.shutdownInstance(computeId);
                    clearSessionForInstance();
                }
            }
        } catch (Exception exception) {
            logger.error("Error shutting down compute instance: {}", exception.getMessage());
            throw new InstanceActionException("Error shutting down a compute instance", exception);
        }
    }
}
