package eu.ill.visa.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "user_role")
public class UserRole {

    @EmbeddedId
    private UserRoleKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"))
    private User user;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_role_id"))
    private Role role;

    @Column(name = "expires_at", nullable = true)
    private Date expiresAt;

    public UserRole() {
    }

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
        this.id = new UserRoleKey(user.getId(), role.getId());
    }

    public UserRole(User user, Role role, Date expiresAt) {
        this.user = user;
        this.role = role;
        this.expiresAt = expiresAt;
        this.id = new UserRoleKey(user.getId(), role.getId());
    }

    public UserRoleKey getId() {
        return id;
    }

    public void setId(UserRoleKey id) {
        this.id = id;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public static class UserRoleKey implements Serializable {
        private Long roleId;
        private String userId;

        public UserRoleKey() {
        }

        public UserRoleKey(String userId, Long roleId) {
            this.roleId = roleId;
            this.userId = userId;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            UserRoleKey that = (UserRoleKey) o;

            return new EqualsBuilder()
                .append(roleId, that.roleId)
                .append(userId, that.userId)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(roleId)
                .append(userId)
                .toHashCode();
        }
    }
}
