package eu.ill.visa.core.domain;

import java.util.Date;

public class ApplicationCredential {

    private Long id;
    private String name;
    private String salt;
    private String applicationId;
    private String applicationSecret;
    private Date deletedAt;

    public ApplicationCredential() {
    }

    public ApplicationCredential(String name, String applicationId, String applicationSecret) {
        this.name = name;
        this.applicationId = applicationId;
        this.applicationSecret = applicationSecret;
    }

    public ApplicationCredential(String name, String salt, String applicationId, String applicationSecret) {
        this.name = name;
        this.salt = salt;
        this.applicationId = applicationId;
        this.applicationSecret = applicationSecret;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
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

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }
}
