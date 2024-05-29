package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.InstanceExtensionRequest;
import eu.ill.visa.core.entity.enumerations.InstanceExtensionRequestState;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

import java.util.Date;

public class InstanceExtensionRequestType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final InstanceType instance;
    private final String comments;
    private final UserType handler;
    private final Date handledOn;
    private final String handlerComments;
    private final Date originalTerminationDate;
    private final Date extensionDate;
    private final InstanceExtensionRequestState state;

    public InstanceExtensionRequestType(final InstanceExtensionRequest request) {
        this.id = request.getId();
        this.instance = new InstanceType(request.getInstance());
        this.comments = request.getComments();
        this.handler = new UserType(request.getHandler());
        this.handledOn = request.getHandledOn();
        this.handlerComments = request.getHandlerComments();
        this.originalTerminationDate = request.getOriginalTerminationDate();
        this.extensionDate = request.getExtensionDate();
        this.state = request.getState();
    }

    public Long getId() {
        return id;
    }

    public InstanceType getInstance() {
        return instance;
    }

    public String getComments() {
        return comments;
    }

    public UserType getHandler() {
        return handler;
    }

    public Date getHandledOn() {
        return handledOn;
    }

    public String getHandlerComments() {
        return handlerComments;
    }

    public Date getOriginalTerminationDate() {
        return originalTerminationDate;
    }

    public Date getExtensionDate() {
        return extensionDate;
    }

    public InstanceExtensionRequestState getState() {
        return state;
    }
}
