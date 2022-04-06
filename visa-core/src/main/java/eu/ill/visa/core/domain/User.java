package eu.ill.visa.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class User implements Serializable {

    private static int DEFAULT_INSTANCE_QUOTA = 2;

    private String id;

    private String firstName;

    private String lastName;

    @JsonIgnore
    private String email;

    private Employer affiliation;

    private List<UserRole> userRoles = new ArrayList<>();

    private Date lastSeenAt;

    private Date activatedAt;

    private Integer instanceQuota;

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return String.format("%s %s", this.firstName, this.lastName);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Employer getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(Employer affiliation) {
        this.affiliation = affiliation;
    }

    public Date getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Date activatedAt) {
        this.activatedAt = activatedAt;
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    @Transient
    public List<UserRole> getActiveUserRoles() {
        long currentTime = new Date().getTime();
        return this.userRoles.stream()
            .filter(userRole -> userRole.getExpiresAt() == null || userRole.getExpiresAt().getTime() > currentTime)
            .collect(Collectors.toList());
    }

    @Transient
    public List<Role> getRoles() {
        return this.getActiveUserRoles().stream().map(UserRole::getRole).collect(Collectors.toList());
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Date lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public void addRole(Role role) {
        UserRole userRole = this.findRole(role.getName());
        if (userRole != null) {
            userRole.setExpiresAt(null);

        } else {
            this.userRoles.add(new UserRole(role));
        }
    }

    public void removeRole(Role role) {
        UserRole userRole = this.findRole(role.getName());
        if (userRole != null) {
            this.userRoles.remove(userRole);
        }
    }

    public boolean hasRole(String targetRole) {
        for (final Role role : this.getRoles()) {
            if (role.getName().equals(targetRole)) {
                return true;
            }
        }
        return false;
    }

    public UserRole findRole(String targetRole) {
        for (final UserRole userRole : userRoles) {
            if (userRole.getRole().getName().equals(targetRole)) {
                return userRole;
            }
        }
        return null;
    }

    public boolean hasAnyRole(List<String> targetRoles) {
        for (final String targetRole : targetRoles) {
            if (this.hasRole(targetRole)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof User) {
            final User other = (User) object;
            return new EqualsBuilder()
                .append(id, other.id)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .toHashCode();
    }

    public Integer getInstanceQuota() {
        return instanceQuota;
    }

    public void setInstanceQuota(Integer instanceQuota) {
        this.instanceQuota = instanceQuota;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("email", email)
            .append("firstName", firstName)
            .append("lastName", lastName)
            .append("affiliation", affiliation)
            .append("activatedAt", activatedAt)
            .append("lastSeenAt", lastSeenAt)
            .append("instanceQuota", instanceQuota)
            .toString();
    }

    public static final class Builder {
        private String  id;
        private String  firstName;
        private String  lastName;
        private String  email;
        private Date    activatedAt;
        private Date    lastSeenAt;
        private Integer instanceQuota = DEFAULT_INSTANCE_QUOTA;

        public Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder activatedAt(Date activatedAt) {
            this.activatedAt = activatedAt;
            return this;
        }

        public Builder lastSeenAt(Date lastSeenAt) {
            this.lastSeenAt = lastSeenAt;
            return this;
        }

        public Builder instanceQuota(Integer instanceQuota) {
            this.instanceQuota = instanceQuota;
            return this;
        }

        public User build() {
            User user = new User();
            user.setId(id);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setActivatedAt(activatedAt);
            user.setLastSeenAt(lastSeenAt);
            user.setInstanceQuota(instanceQuota);
            return user;
        }

    }
}
