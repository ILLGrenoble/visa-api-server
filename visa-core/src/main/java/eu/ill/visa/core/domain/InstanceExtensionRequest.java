package eu.ill.visa.core.domain;

import eu.ill.visa.core.domain.enumerations.InstanceExtensionRequestState;

import java.util.Date;

public class InstanceExtensionRequest extends Timestampable {

    private Long id;

    private Instance instance;

    private String comments;

    private User handler;

    private Date handledOn;

    private String handlerComments;

    private Date originalTerminationDate;

    private Date extensionDate;

    private InstanceExtensionRequestState state;

    public InstanceExtensionRequest() {
    }

    public InstanceExtensionRequest(Instance instance, String comments) {
        this.instance = instance;
        this.comments = comments;
        this.state = InstanceExtensionRequestState.PENDING;
        this.originalTerminationDate = instance.getTerminationDate();
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

    public String getHandlerComments() {
        return handlerComments;
    }

    public void setHandlerComments(String handlerComments) {
        this.handlerComments = handlerComments;
    }

    public InstanceExtensionRequestState getState() {
        return state;
    }

    public void setState(InstanceExtensionRequestState state) {
        this.state = state;
    }

    public Date getOriginalTerminationDate() {
        return originalTerminationDate;
    }

    public void setOriginalTerminationDate(Date originalTerminationDate) {
        this.originalTerminationDate = originalTerminationDate;
    }

    public Date getExtensionDate() {
        return extensionDate;
    }

    public void setExtensionDate(Date extensionDate) {
        this.extensionDate = extensionDate;
    }
}
