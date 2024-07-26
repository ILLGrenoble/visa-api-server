package eu.ill.visa.core.entity;

import jakarta.persistence.*;

import java.util.Calendar;
import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "clientAuthenticationToken.getByTokenAndClientId", query = """
            SELECT c FROM ClientAuthenticationToken c
            WHERE c.token = :token
            AND c.clientId = :clientId
    """),
    @NamedQuery(name = "clientAuthenticationToken.getAll", query = """
            SELECT c FROM InstanceAuthenticationToken c
    """),
})
@Table(name = "client_authentication_token")
public class ClientAuthenticationToken extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "token", length = 250, nullable = false)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = false)
    private User user;

    @Column(name = "client_id", length = 250, nullable = false)
    private String clientId;

    public ClientAuthenticationToken() {

    }

    private ClientAuthenticationToken(Builder builder) {
        this.id = builder.id;
        this.token = builder.token;
        this.user = builder.user;
        this.clientId = builder.clientId;
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

    public String getClientId() {
        return clientId;
    }

    public static final class Builder {
        private Long   id;
        private String token;
        private User   user;
        private String clientId;
        private Builder() {
        }

        public ClientAuthenticationToken build() {
            return new ClientAuthenticationToken(this);
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }
    }
}
