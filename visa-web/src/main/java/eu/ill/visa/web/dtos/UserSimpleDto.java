package eu.ill.visa.web.dtos;

import java.util.ArrayList;
import java.util.List;

public class UserSimpleDto {
    private String id;
    private String fullName;
    private String  firstName;
    private String  lastName;
    private EmployerDto  affiliation;
    private List<RoleDto> userRoles = new ArrayList<>();

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

    public EmployerDto getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(EmployerDto affiliation) {
        this.affiliation = affiliation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<RoleDto> getUserRoles() {
        return userRoles;
    }

    public void addUserRole(RoleDto userRole) {
        this.userRoles.add(userRole);
    }
}
