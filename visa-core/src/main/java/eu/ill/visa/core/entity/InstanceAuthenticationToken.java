package eu.ill.visa.core.entity;

import jakarta.persistence.*;

import java.util.Calendar;
import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceAuthenticationToken.getByToken", query = """
            SELECT i FROM InstanceAuthenticationToken i
            WHERE i.token = :token
            AND i.instance.deletedAt IS NULL
    """),
    @NamedQuery(name = "instanceAuthenticationToken.getAll", query = """
            SELECT i FROM InstanceAuthenticationToken i
            WHERE i.instance.deletedAt IS NULL
    """),
})
@Table(name = "instance_authentication_token")
public class InstanceAuthenticationToken extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "token", length = 250, nullable = false)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private Instance instance;

    @Column(name = "public_access_token", length = 250, nullable = true)
    private String publicAccessToken;

    public InstanceAuthenticationToken() {

    }

    private InstanceAuthenticationToken(Builder builder) {
        this.token = builder.token;
        this.user = builder.user;
        this.instance = builder.instance;
        this.publicAccessToken = builder.publicAccessToken;
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

    public Boolean isExpired(int deltaSeconds) {
        final Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.createdAt);
        calendar.add(Calendar.SECOND, deltaSeconds);

        return calendar.getTime().before(now);
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

    public String getPublicAccessToken() {
        return publicAccessToken;
    }

    public void setPublicAccessToken(String accessToken) {
        this.publicAccessToken = accessToken;
    }

    public static final class Builder {
        private String token;
        private User   user;
        private Instance instance;
        private String publicAccessToken;

        private Builder() {
        }

        public InstanceAuthenticationToken build() {
            return new InstanceAuthenticationToken(this);
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder instance(Instance instance) {
            this.instance = instance;
            return this;
        }

        public Builder publicAccessToken(String accessToken) {
            this.publicAccessToken = accessToken;
            return this;
        }
    }
}
