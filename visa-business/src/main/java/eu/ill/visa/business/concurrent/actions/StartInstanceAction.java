package eu.ill.visa.business.concurrent.actions;

import eu.ill.visa.business.concurrent.actions.exceptions.InstanceActionException;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartInstanceAction extends InstanceAction {

    private final static Logger logger = LoggerFactory.getLogger(StartInstanceAction.class);

    public StartInstanceAction(InstanceActionServiceProvider serviceProvider, InstanceCommand command) {
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
                logger.info("Compute instance {} does not have a compute id associated to it. Ignoring start action", instance.getId());
            } else {
                final CloudInstance cloudInstance = cloudClient.instance(computeId);
                if (cloudInstance == null) {
                    logger.info("Could not find compute instance: {}", computeId);
                } else {
                    logger.info("Starting compute instance: {}", instance.getId());
                    cloudClient.startInstance(computeId);
                    clearSessionForInstance();
                }
            }
        } catch (Exception exception) {
            logger.error("Error starting compute instance: {}", exception.getMessage());
            throw new InstanceActionException("Error starting a compute instance", exception);
        }
    }
}
