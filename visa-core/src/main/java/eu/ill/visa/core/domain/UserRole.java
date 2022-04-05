package eu.ill.visa.core.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

public class UserRole {
    private UserRoleKey id;

    private User user;
    private Role role;
    private Date expiresAt;

    public UserRole() {
    }

    public UserRole(Role role) {
        this.role = role;
    }

    public UserRole(Role role, Date expiresAt) {
        this.role = role;
        this.expiresAt = expiresAt;
    }

    public UserRoleKey getId() {
        return id;
    }

    public void setId(UserRoleKey id) {
        this.id = id;
    }

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

        public UserRoleKey(Long roleId, String userId) {
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
