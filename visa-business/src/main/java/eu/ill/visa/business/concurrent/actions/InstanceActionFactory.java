package eu.ill.visa.business.concurrent.actions;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;

@Singleton
public class InstanceActionFactory {

    private InstanceActionServiceProvider serviceProvider;

    @Inject
    public InstanceActionFactory(InstanceActionServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public InstanceAction create(InstanceCommand command) {
        InstanceAction action = null;
        if (command.getActionType().equals(InstanceCommandType.START)) {
            action = new StartInstanceAction(this.serviceProvider, command);

        } else if (command.getActionType().equals(InstanceCommandType.SHUTDOWN)) {
            action = new ShutdownInstanceAction(this.serviceProvider, command);

        } else if (command.getActionType().equals(InstanceCommandType.STATE)) {
            action = new StateInstanceAction(this.serviceProvider, command);

        } else if (command.getActionType().equals(InstanceCommandType.REBOOT)) {
            action = new RebootInstanceAction(this.serviceProvider, command);

        } else if (command.getActionType().equals(InstanceCommandType.CREATE)) {
            action = new CreateInstanceAction(this.serviceProvider, command);

        } else if (command.getActionType().equals(InstanceCommandType.DELETE)) {
            action = new DeleteInstanceAction(this.serviceProvider, command);

        } else if (command.getActionType().equals(InstanceCommandType.UPDATE_SECURITY_GROUPS)) {
            action = new UpdateInstanceSecurityGroupsAction(this.serviceProvider, command);
        }

        return action;
    }
}
