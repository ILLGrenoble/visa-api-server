package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.User;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;
import java.util.List;

@Type("User")
public class UserType {

    private final @NotNull String id;
    private final String firstName;
    private final String lastName;
    private final String fullName;
    private final String email;
    private final EmployerType affiliation;
    private final @NotNull Integer instanceQuota;
    private final List<UserRoleType> activeUserRoles;
    private final List<RoleType> groups;
    private final Date lastSeenAt;
    private final Date activatedAt;

    public UserType(final User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.affiliation = user.getAffiliation() == null ? null : new EmployerType(user.getAffiliation());
        this.instanceQuota = user.getInstanceQuota();
        this.activeUserRoles = user.getActiveUserRoles().stream().map(UserRoleType::new).toList();
        this.groups = user.getGroups().stream().map(RoleType::new).toList();
        this.lastSeenAt = user.getLastSeenAt();
        this.activatedAt = user.getActivatedAt();
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public EmployerType getAffiliation() {
        return affiliation;
    }

    public Integer getInstanceQuota() {
        return instanceQuota;
    }

    public List<UserRoleType> getActiveUserRoles() {
        return activeUserRoles;
    }

    public List<RoleType> getGroups() {
        return groups;
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public Date getActivatedAt() {
        return activatedAt;
    }
}
