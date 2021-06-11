package eu.ill.visa.business.concurrent.actions;

import eu.ill.visa.business.concurrent.actions.exceptions.InstanceActionException;
import eu.ill.visa.business.services.*;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.enumerations.InstanceCommandState;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class InstanceAction {

    private InstanceActionServiceProvider serviceProvider;
    private InstanceCommand command;

    public InstanceAction(InstanceActionServiceProvider serviceProvider, InstanceCommand command) {
        this.serviceProvider = serviceProvider;
        this.command = command;
    }

    public CloudClient getCloudClient() {
        return this.serviceProvider.getCloudClient();
    }

    public NotificationService getNotificationService() {
        return this.serviceProvider.getNotificationService();
    }

    public InstanceService getInstanceService() {
        return this.serviceProvider.getInstanceService();
    }

    public InstanceCommandService getInstanceCommandService() {
        return this.serviceProvider.getInstanceCommandService();
    }

    public SecurityGroupService getSecurityGroupService() {
        return this.serviceProvider.getSecurityGroupService();
    }

    public InstrumentService getInstrumentService() {
        return this.serviceProvider.getInstrumentService();
    }

    public InstanceCommand getCommand() {
        return command;
    }

    public Instance getInstance() {
        return this.getInstanceService().getById(command.getInstance().getId());
    }

    public InstanceCommandState getCommandStateFromDatabase() {
        Long commandId = this.command.getId();
        if (commandId != null) {
            InstanceCommandService commandService = this.getInstanceCommandService();
            InstanceCommandState commandState = commandService.getById(commandId).getState();
            this.command.setState(commandState);
        }

        return this.command.getState();
    }

    public void updateInstanceState(InstanceState instanceState) {
        Instance commandInstance = this.command.getInstance();
        commandInstance.setState(instanceState);

        Instance instance = this.getInstance();
        if (instance != null) {
            instance.setState(instanceState);

            // Soft delete if necessary
            if (instanceState.equals(InstanceState.DELETED)) {
                instance.setDeleted(true);
            }

            this.getInstanceService().save(instance);
        }
    }

    public void updateInstanceIpAddress(String ipAddress) {
        Instance commandInstance = this.command.getInstance();
        commandInstance.setIpAddress(ipAddress);

        final Instance instance = this.getInstance();
        if (instance != null) {
            instance.setIpAddress(ipAddress);
            this.getInstanceService().save(instance);
        }
    }

    public void clearSessionForInstance() {
        final Instance instance = this.getInstance();
        if (instance != null) {
            this.serviceProvider.clearSessionsForInstance(instance);
        }
    }

    public abstract void run() throws InstanceActionException;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InstanceAction that = (InstanceAction) o;

        return new EqualsBuilder()
            .append(command, that.command)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(command)
            .toHashCode();
    }
}
