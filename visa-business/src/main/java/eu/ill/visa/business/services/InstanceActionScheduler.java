package eu.ill.visa.business.services;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;
import eu.ill.visa.core.domain.enumerations.InstanceState;

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
