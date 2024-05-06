package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.enumerations.InstanceCommandState;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;

import java.util.Date;

public class InstanceCommandDto {

    private Long id;
    private InstanceCommandType actionType;
    private InstanceCommandState state;
    private String message;
    private Date createdAt;

    public InstanceCommandDto() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
