package eu.ill.visa.business.concurrent.actions;

import eu.ill.visa.business.concurrent.actions.exceptions.InstanceActionException;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.domain.CloudInstanceMetadata;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class CreateInstanceAction extends InstanceAction {

    private final static Logger logger = LoggerFactory.getLogger(CreateInstanceAction.class);

    public CreateInstanceAction(InstanceActionServiceProvider serviceProvider, InstanceCommand command) {
        super(serviceProvider, command);
    }

    @Override
    public void run() throws InstanceActionException {
        final Instance instance = getInstance();
        if (instance == null) {
            return;
        }

        try {
            final CloudClient cloudClient = this.getCloudClient(instance.getCloudId());
            if (cloudClient == null) {
                return;
            }

            final Plan plan = instance.getPlan();
            final Flavour flavour = plan.getFlavour();
            final Image image = plan.getImage();

            // Get security groups for instance
            List<String> securityGroupNames = this.getSecurityGroupService().getAllSecurityGroupNamesForInstance(instance);
            logger.info("Adding security groups [{}] to instance {}", String.join(", ", securityGroupNames), instance.getId());

            instance.setSecurityGroups(securityGroupNames);

            User owner = instance.getOwner().getUser();
            List<Instrument> instruments = this.getInstrumentService().getAllForExperimentsAndInstrumentScientist(instance.getExperiments(), owner);
            List<String> instrumentNames = instruments.stream().map(Instrument::getName).collect(Collectors.toUnmodifiableList());

            List<String> proposals = instance.getExperiments().stream().map(experiment -> experiment.getProposal().getIdentifier()).collect(Collectors.toUnmodifiableList());

            final CloudInstanceMetadata metadata = new CloudInstanceMetadata();

            instance.getAttributes().forEach(attribute -> metadata.put(attribute.getName(), attribute.getValue()));
            metadata.put("id", instance.getId());
            metadata.put("uid", instance.getUid());
            metadata.put("owner", instance.getUsername());
            metadata.put("instruments", String.join(",", instrumentNames));
            metadata.put("proposals", String.join(",", proposals));

            String pamPublicKey = this.getSignatureService().readPublicKey();
            if (pamPublicKey != null) {
                metadata.put("pamPublicKey", pamPublicKey);
            }

            CloudInstance cloudInstance = cloudClient.createInstance(
                cloudClient.getServerNamePrefix() + "-" + instance.getId(),
                image.getComputeId(),
                flavour.getComputeId(),
                securityGroupNames,
                metadata,
                image.getBootCommand()
            );

            instance.setComputeId(cloudInstance.getId());
            InstanceState instanceState = InstanceState.valueOf(cloudInstance.getState().toString());

            instance.setState(instanceState);

            this.getInstanceService().save(instance);
            this.getEmailManager().sendInstanceCreatedNotification(instance);

        } catch (CloudException exception) {
            this.updateInstanceState(InstanceState.ERROR);
            logger.error("Error creating a new compute instance for instance {}: {}", instance.getId(), exception.getMessage());
            throw new InstanceActionException("Error creating a compute instance", exception);
        }
    }
}
