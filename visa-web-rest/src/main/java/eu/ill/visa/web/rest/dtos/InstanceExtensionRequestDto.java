package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.InstanceExtensionRequest;

import java.util.Date;

public class InstanceExtensionRequestDto {

    private final Long id;
    private final String comments;
    private final Date createdAt;

    public InstanceExtensionRequestDto(final InstanceExtensionRequest request) {
        this.id = request.getId();
        this.comments = request.getComments();
        this.createdAt = request.getCreatedAt();
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
}
