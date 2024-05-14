package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

@Transactional
@Singleton
public class InstanceActionScheduler {

    private final InstanceService instanceService;
    private final InstanceCommandService instanceCommandService;

    @Inject
    public InstanceActionScheduler(final InstanceService instanceService, final InstanceCommandService instanceCommandService) {
        this.instanceService = instanceService;
        this.instanceCommandService = instanceCommandService;
    }

    public void execute(Instance instance, User user, InstanceCommandType instanceCommandType) {
        final InstanceState state = switch (instanceCommandType) {
            case START -> InstanceState.STARTING;
            case REBOOT -> InstanceState.REBOOTING;
            case SHUTDOWN -> InstanceState.STOPPING;
            case DELETE -> InstanceState.DELETING;
            default -> throw new IllegalArgumentException("Invalid state provided");
        };

        instance.setState(state);
        instanceService.save(instance);

        // Create the command and let the scheduler manage the execution
        instanceCommandService.create(user, instance, instanceCommandType);
    }
}
