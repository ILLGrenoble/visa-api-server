package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "personalAccessToken.getByInstanceAndId", query = """
        SELECT pat FROM PersonalAccessToken pat
        WHERE pat.instance = :instance
        AND pat.id = :id
        AND pat.user IS NULL
        AND pat.deletedAt IS NULL
        AND pat.instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "personalAccessToken.getByInstanceAndToken", query = """
        SELECT pat FROM PersonalAccessToken pat
        WHERE pat.instance = :instance
        AND pat.token = :token
        AND pat.user IS NULL
        AND pat.deletedAt IS NULL
        AND pat.instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "personalAccessToken.getAllForInstance", query = """
        SELECT pat FROM PersonalAccessToken pat
        WHERE pat.instance = :instance
        AND pat.user IS NULL
        AND pat.deletedAt IS NULL
        AND pat.instance.deletedAt IS NULL
    """),
})
@Table(name = "personal_access_token")
public class PersonalAccessToken extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "token", length = 250, nullable = false)
    private String token;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = true)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private Instance instance;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 255, nullable = true)
    private InstanceMemberRole role;

    @Column(name = "activated_at", nullable = true)
    private Date activatedAt;

    @Column(name = "deleted_at", nullable = true)
    private Date deletedAt;

    public PersonalAccessToken() {

    }

    private PersonalAccessToken(Builder builder) {
        this.token = builder.token;
        this.name = builder.name;
        this.role = builder.role;
        this.instance = builder.instance;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public void setRole(InstanceMemberRole role) {
        this.role = role;
    }

    public Date getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Date activatedAt) {
        this.activatedAt = activatedAt;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public static final class Builder {
        private String token;
        private String name;
        private Instance instance;
        private InstanceMemberRole role;

        private Builder() {
        }

        public PersonalAccessToken build() {
            return new PersonalAccessToken(this);
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder role(InstanceMemberRole role) {
            this.role = role;
            return this;
        }

        public Builder instance(Instance instance) {
            this.instance = instance;
            return this;
        }
    }
}
