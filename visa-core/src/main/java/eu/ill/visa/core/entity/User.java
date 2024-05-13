package eu.ill.visa.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@NamedQueries({
    @NamedQuery(name = "user.getById", query = """
            SELECT u
            FROM User u
            WHERE u.id = :id
    """),
    @NamedQuery(name = "user.getByIdWithRoles", query = """
            SELECT u
            FROM User u
            LEFT OUTER JOIN FETCH u.userRoles
            WHERE u.id = :id
    """),
    @NamedQuery(name = "user.getAll", query = """
            SELECT u
            FROM User u
            ORDER BY u.lastName ASC
    """),
    @NamedQuery(name = "user.getAllActivated", query = """
            SELECT u
            FROM User u
            WHERE u.activatedAt IS NOT NULL
            ORDER BY u.lastName ASC
    """),
    @NamedQuery(name = "user.countAll", query = """
            SELECT COUNT(u)
            FROM User u
    """),
    @NamedQuery(name = "user.countAllUsersForRole", query = """
            SELECT COUNT(u) FROM User u
            JOIN u.userRoles ur
            JOIN ur.role r
            WHERE r.name = :role
    """),
    @NamedQuery(name = "user.getAllLikeLastName", query = """
            SELECT DISTINCT(u)
            FROM User u
            WHERE LOWER(u.lastName) LIKE LOWER(:lastName) || '%'
            ORDER BY u.lastName, u.firstName ASC
    """),
    @NamedQuery(name = "user.getAllActivatedLikeLastName", query = """
            SELECT DISTINCT(u)
            FROM User u
            WHERE LOWER(u.lastName) LIKE LOWER(:lastName) || '%'
            AND u.activatedAt IS NOT NULL
            ORDER BY u.lastName, u.firstName ASC
    """),
    @NamedQuery(name = "user.countAllLikeLastName", query = """
            SELECT COUNT(u)
            FROM User u
            WHERE LOWER(u.lastName) LIKE LOWER(:lastName) || '%'
    """),
    @NamedQuery(name = "user.countAllActivatedLikeLastName", query = """
            SELECT COUNT(u)
            FROM User u
            WHERE LOWER(u.lastName) LIKE LOWER(:lastName) || '%'
            AND u.activatedAt IS NOT NULL
    """),
    @NamedQuery(name = "user.getAllStaff", query = """
            SELECT DISTINCT u
            FROM User u
            JOIN u.userRoles ur
            JOIN ur.role r
            WHERE r.name = 'STAFF'
            ORDER BY u.lastName
    """),
    @NamedQuery(name = "user.getAllSupport", query = """
            SELECT DISTINCT u
            FROM User u
            LEFT OUTER JOIN FETCH u.userRoles ur
            JOIN ur.role r
            WHERE r.name in ('INSTRUMENT_SCIENTIST', 'INSTRUMENT_CONTROL', 'IT_SUPPORT', 'SCIENTIFIC_COMPUTING')
            ORDER BY u.lastName
    """),
    @NamedQuery(name = "user.countAllActivated", query = """
            SELECT count(distinct u.id)
            FROM User u
            WHERE u.activatedAt IS NOT NULL
    """),
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "user.getExperimentalTeamForInstance", resultClass = User.class, query = """
            SELECT DISTINCT u.id, u.affiliation_id, u.email, u.first_name, u.last_name, u.activated_at, u.last_seen_at, u.instance_quota
            FROM users u
            LEFT JOIN experiment_user eu ON eu.user_id = u.id
            WHERE eu.experiment_id IN (
                SELECT i.experiment_id
                FROM instance_experiment i
                WHERE i.instance_id = ?1
            )
            ORDER BY u.last_name
    """),
})
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "first_name", length = 100, nullable = true)
    private String firstName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @JsonIgnore
    @Column(name = "email", length = 100, nullable = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "affiliation_id", foreignKey = @ForeignKey(name = "fk_affiliation_id"), nullable = true)
    private Employer affiliation;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = false, insertable = false, updatable = false)
    private List<UserRole> userRoles = new ArrayList<>();

    @Column(name = "last_seen_at", nullable = true)
    private Date lastSeenAt;

    @Column(name = "activated_at", nullable = true)
    private Date activatedAt;

    @Column(name = "instance_quota", nullable = false)
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

    @Transient
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
            .filter(userRole -> userRole.getRole().getGroupCreatedAt() == null)
            .filter(userRole -> userRole.getRole().getGroupDeletedAt() == null)
            .filter(userRole -> userRole.getExpiresAt() == null || userRole.getExpiresAt().getTime() > currentTime)
            .collect(Collectors.toList());
    }

    @Transient
    public List<Role> getRoles() {
        return this.getActiveUserRoles().stream().map(UserRole::getRole).collect(Collectors.toList());
    }

    @Transient
    public List<Role> getGroups() {
        return this.userRoles.stream()
            .map(UserRole::getRole)
            .filter(role -> role.getGroupCreatedAt() != null)
            .filter(role -> role.getGroupDeletedAt() == null)
            .collect(Collectors.toList());
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Date lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public void addRole(Role role) {
        this.addRole(role, null);
    }

    public void addRole(Role role, Date expiresAt) {
        UserRole userRole = this.findRole(role.getName());
        if (userRole != null) {
            userRole.setExpiresAt(expiresAt);

        } else {
            this.userRoles.add(new UserRole(this, role, expiresAt));
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
        private Integer instanceQuota; // Default quota now provided by the UserService

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
