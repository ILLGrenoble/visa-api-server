package eu.ill.visa.web.bundles.graphql.queries.domain;

import eu.ill.visa.core.domain.ApplicationCredential;

public class ApplicationCredentialDetail {

    private Long id;
    private String name;
    private String applicationId;

    public ApplicationCredentialDetail(ApplicationCredential applicationCredential) {
        this.id = applicationCredential.getId();
        this.name = applicationCredential.getName();
        this.applicationId = applicationCredential.getApplicationId();
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
}
