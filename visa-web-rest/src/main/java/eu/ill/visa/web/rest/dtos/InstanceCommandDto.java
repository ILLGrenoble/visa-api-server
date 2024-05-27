package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.InstanceCommand;
import eu.ill.visa.core.entity.enumerations.InstanceCommandState;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;

import java.util.Date;

public class InstanceCommandDto {

    private final Long id;
    private final InstanceCommandType actionType;
    private final InstanceCommandState state;
    private final String message;
    private final Date createdAt;

    public InstanceCommandDto(final InstanceCommand instanceCommand) {
        this.id = instanceCommand.getId();
        this.actionType = instanceCommand.getActionType();
        this.state = instanceCommand.getState();
        this.message = instanceCommand.getMessage();
        this.createdAt = instanceCommand.getCreatedAt();
    }


    public Long getId() {
        return id;
    }

    public InstanceCommandType getActionType() {
        return actionType;
    }

    public InstanceCommandState getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
