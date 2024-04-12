package eu.ill.visa.business.concurrent;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import eu.ill.visa.business.concurrent.actions.InstanceAction;
import eu.ill.visa.business.concurrent.actions.InstanceActionFactory;
import eu.ill.visa.business.services.InstanceCommandService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.enumerations.InstanceCommandState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Optional;

@Singleton
public class InstanceActionManager implements InstanceActionListener {

    private final InstanceActionDispatcher dispatcher;
    private final InstanceCommandService instanceCommandService;
    private final InstanceActionFactory instanceActionFactory;

    private final HashMap<Instance, Deque<InstanceActionFuture>> actions = new HashMap<>();

    @Inject
    public InstanceActionManager(final InstanceActionDispatcher dispatcher,
                                 final InstanceCommandService instanceCommandService,
                                 final InstanceActionFactory instanceActionFactory) {
        this.dispatcher = dispatcher;
        this.instanceCommandService = instanceCommandService;
        this.instanceActionFactory = instanceActionFactory;
    }

    public synchronized InstanceActionFuture queue(InstanceCommand command) {
        InstanceAction action = this.instanceActionFactory.create(command);
        if (action == null) {
            throw new RuntimeException("Instance command " + command.toString() + " not mapped to an action");
        }
        return this.queue(action);
    }

    public InstanceActionFuture queue(InstanceAction action) {
        InstanceActionFuture instanceActionFuture = null;
        boolean doExecute = true;

        synchronized (this) {
            InstanceCommand command = action.getCommand();
            Instance instance = command.getInstance();

            if (this.actions.containsKey(instance)) {
                // Just queue action
                doExecute = false;

            } else {
                // Create queue of actions
                this.actions.put(instance, new ArrayDeque<>());
            }

            // Queue action
            Deque<InstanceActionFuture> actionQueue = this.actions.get(instance);

            // Encapsulate action in a runner to handle notifications
            InstanceActionRunner actionRunner = new InstanceActionRunner(action, this);

            // Verify that command type doesn't already exist for the instance
            Optional<InstanceActionFuture> actionOptional = actionQueue.stream().filter(actionFutureTask -> actionFutureTask.getRunner().getCommand().getActionType() == action.getCommand().getActionType()).findAny();
            if (actionOptional.isPresent()) {
                instanceActionFuture = actionOptional.get();

                command.setState(InstanceCommandState.TERMINATED);
                this.instanceCommandService.save(command);

            } else {
                // Store runner in a future
                instanceActionFuture = new InstanceActionFuture(this.dispatcher, actionRunner);

                actionQueue.add(instanceActionFuture);

                command.setState(InstanceCommandState.QUEUED);
                this.instanceCommandService.save(command);
            }
        }

        // Execute if first on queue
        if (doExecute) {
            instanceActionFuture.run();
        }

        return instanceActionFuture;
    }

    public synchronized void cancel(InstanceCommand command) {
        command.setState(InstanceCommandState.CANCELLED);
        this.instanceCommandService.save(command);

        // Find the action corresponding to the command
        Instance instance = command.getInstance();

        if (this.actions.get(instance) != null) {
            Deque<InstanceActionFuture> actionQueue = this.actions.get(instance);

            Optional<InstanceActionFuture> actionOptional = actionQueue.stream().filter(actionFutureTask -> actionFutureTask.getRunner().getCommand().equals(command)).findAny();

            if (actionOptional.isPresent()) {
                InstanceActionFuture instanceActionFuture = actionOptional.get();

                // Remove action from the queue
                actionQueue.removeIf(actionFutureTask -> actionFutureTask.getRunner().getCommand().equals(command));

                // Cancel a future instance action (will stop if from running if not already started but will not stop a started runner)
                instanceActionFuture.cancel(false);
            }
        }
    }

    private synchronized void handleNextActionForInstance(Instance instance) {
        if (this.actions.containsKey(instance)) {
            Deque<InstanceActionFuture> actionQueue = this.actions.get(instance);

            // Remove first action
            if (actionQueue.size() > 0) {
                actionQueue.removeFirst();
            }

            if (actionQueue.size() > 0) {
                var instanceActionFutureTask = actionQueue.getFirst();
                instanceActionFutureTask.run();

            } else {
                // Remove instance queue
                this.actions.remove(instance);
            }
        }
    }

    @Override
    public synchronized void onActionStart(InstanceAction action) {
        InstanceCommand command = action.getCommand();

        command.setState(InstanceCommandState.RUNNING);
        this.instanceCommandService.save(command);
    }

    @Override
    public synchronized void onActionTerminated(InstanceAction action) {
        InstanceCommand command = action.getCommand();

        // Verify command hasn't been cancelled
        if (!action.getCommandStateFromDatabase().equals(InstanceCommandState.CANCELLED)) {
            command.setState(InstanceCommandState.TERMINATED);
            this.instanceCommandService.save(command);
        }

        this.handleNextActionForInstance(action.getCommand().getInstance());
    }

    @Override
    public synchronized void onActionFailed(InstanceAction action) {
        InstanceCommand command = action.getCommand();

        // Verify command hasn't been cancelled
        if (!action.getCommandStateFromDatabase().equals(InstanceCommandState.CANCELLED)) {
            command.setState(InstanceCommandState.FAILED);
            this.instanceCommandService.save(command);
        }

        this.handleNextActionForInstance(action.getCommand().getInstance());
    }
}
