package eu.ill.visa.business.concurrent;

import eu.ill.visa.business.concurrent.actions.InstanceAction;
import eu.ill.visa.business.concurrent.actions.exceptions.InstanceActionException;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceCommand;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class InstanceActionRunner implements Callable<Instance> {

    private final static Logger logger = LoggerFactory.getLogger(InstanceActionRunner.class);

    private final InstanceAction action;
    private final InstanceActionListener listener;

    public InstanceActionRunner(final InstanceAction action,
                                final InstanceActionListener listener) {
        this.action = action;
        this.listener = listener;
    }

    public InstanceCommand getCommand() {
        return this.action.getCommand();
    }

    @Override
    public Instance call() throws Exception {

        try {
            this.listener.onActionStart(this.action);

            this.action.run();

            this.listener.onActionTerminated(this.action);

            return this.action.getInstance();

        } catch (Exception exception) {
            if (!(exception instanceof InstanceActionException)) {
                logger.error("Error running action on instance {} : {}", this.action.getCommand().getInstance().getId(), exception.getMessage());
            }
            this.action.getCommand().setMessage("Action failed: " + exception.getMessage());
            this.listener.onActionFailed(this.action);

            throw exception;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InstanceActionRunner that = (InstanceActionRunner) o;

        return new EqualsBuilder()
            .append(action, that.action)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(action)
            .toHashCode();
    }
}
