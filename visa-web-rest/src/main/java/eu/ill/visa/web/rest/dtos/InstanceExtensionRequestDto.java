package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.InstanceExtensionRequest;
import eu.ill.visa.core.entity.enumerations.InstanceExtensionRequestState;

import java.util.Date;

public class InstanceExtensionRequestDto {

    private final Long id;
    private final String comments;
    private final Date createdAt;
    private final InstanceExtensionRequestState state;

    public InstanceExtensionRequestDto(final InstanceExtensionRequest request) {
        this.id = request.getId();
        this.comments = request.getComments();
        this.createdAt = request.getCreatedAt();
        this.state = request.getState();
    }

    public Long getId() {
        return id;
    }

    public String getComments() {
        return comments;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public InstanceExtensionRequestState getState() {
        return state;
    }
}
