package eu.ill.visa.core.domain;

import eu.ill.visa.core.domain.enumerations.InstanceExtensionRequestState;

import java.util.Date;

public class InstanceExtensionRequest extends Timestampable {

    private Long id;

    private Instance instance;

    private String comments;

    private User handler;

    private Date handledOn;

    private InstanceExtensionRequestState state;

    public InstanceExtensionRequest() {
    }

    public InstanceExtensionRequest(Instance instance, String comments) {
        this.instance = instance;
        this.comments = comments;
        this.state = InstanceExtensionRequestState.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public User getHandler() {
        return handler;
    }

    public void setHandler(User handler) {
        this.handler = handler;
    }

    public Date getHandledOn() {
        return handledOn;
    }

    public void setHandledOn(Date handledOn) {
        this.handledOn = handledOn;
    }

    public InstanceExtensionRequestState getState() {
        return state;
    }

    public void setState(InstanceExtensionRequestState state) {
        this.state = state;
    }
}
