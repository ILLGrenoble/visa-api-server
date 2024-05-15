package eu.ill.visa.web.graphql.queries.domain;

import eu.ill.visa.core.entity.ApplicationCredential;

import java.util.Date;

public class ApplicationCredentialDetail {

    private Long id;
    private String name;
    private String applicationId;
    private Date lastUsedAt;

    public ApplicationCredentialDetail(ApplicationCredential applicationCredential) {
        this.id = applicationCredential.getId();
        this.name = applicationCredential.getName();
        this.applicationId = applicationCredential.getApplicationId();
        this.lastUsedAt = applicationCredential.getLastUsedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Date getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Date lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
