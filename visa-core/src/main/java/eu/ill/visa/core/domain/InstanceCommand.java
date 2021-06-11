package eu.ill.visa.core.domain;

import eu.ill.visa.core.domain.enumerations.InstanceCommandState;
import eu.ill.visa.core.domain.enumerations.InstanceCommandType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class InstanceCommand extends Timestampable {

    private Long id;
    private User user;
    private Instance instance;
    private InstanceCommandType actionType;
    private InstanceCommandState state;
    private String message;

    public InstanceCommand() {
    }

    public InstanceCommand(User user, Instance instance, InstanceCommandType actionType) {
        this.user = user;
        this.instance = instance;
        this.actionType = actionType;
        this.state = InstanceCommandState.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public InstanceCommandType getActionType() {
        return actionType;
    }

    public void setActionType(InstanceCommandType actionType) {
        this.actionType = actionType;
    }

    public InstanceCommandState getState() {
        return state;
    }

    public void setState(InstanceCommandState state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InstanceCommand that = (InstanceCommand) o;

        return new EqualsBuilder()
            .append(user, that.user)
            .append(instance, that.instance)
            .append(actionType, that.actionType)
            .append(state, that.state)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(user)
            .append(instance)
            .append(actionType)
            .append(state)
            .toHashCode();
    }
}
