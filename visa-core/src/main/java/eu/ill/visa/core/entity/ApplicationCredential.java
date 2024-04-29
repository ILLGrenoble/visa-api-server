package eu.ill.visa.core.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "application_credential")
public class ApplicationCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @Column(name = "salt", nullable = false)
    private String salt;

    @Column(name = "application_id", nullable = false)
    private String applicationId;

    @Column(name = "application_secret", nullable = false)
    private String applicationSecret;

    @Column(name = "last_used_at")
    private Date lastUsedAt;

    @Column(name = "deleted_at")
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

    public Date getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Date lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }
}
