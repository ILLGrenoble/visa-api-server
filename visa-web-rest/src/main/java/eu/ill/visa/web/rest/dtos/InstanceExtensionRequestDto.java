package eu.ill.visa.web.rest.dtos;

import java.util.Date;

public class InstanceExtensionRequestDto {

    private Long id;

    private String comments;

    private Date createdAt;

    public InstanceExtensionRequestDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
