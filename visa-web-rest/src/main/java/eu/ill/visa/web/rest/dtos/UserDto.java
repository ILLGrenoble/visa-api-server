package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserDto {
    private final String id;
    private final String fullName;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final EmployerDto affiliation;
    private final List<RoleDto> activeUserRoles = new ArrayList<>();
    private final List<String> groups = new ArrayList<>();

    public UserDto(final User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.affiliation = user.getAffiliation() == null ? null : new EmployerDto(user.getAffiliation());
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public EmployerDto getAffiliation() {
        return affiliation;
    }

    public List<RoleDto> getActiveUserRoles() {
        return activeUserRoles;
    }

    public void addActiveUserRole(RoleDto userRole) {
        this.activeUserRoles.add(userRole);
    }

    public List<String> getGroups() {
        return groups;
    }

    public void addGroup(String group) {
        this.groups.add(group);
    }
}
