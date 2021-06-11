package eu.ill.visa.web.dtos;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class UserFullDto {
    private String id;
    private String fullName;
    private String   firstName;
    private String   lastName;
    private String   email;
    private DateTime lastSignInAt;
    private DateTime createdAt;
    private String   lastSignInIp;
    private Integer  signInCount;
    private Integer  machineQuotaLimit;
    private EmployerDto affiliation;
    private List<String> roles = new ArrayList<>();

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


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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


    public DateTime getLastSignInAt() {
        return lastSignInAt;
    }

    public void setLastSignInAt(DateTime lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastSignInIp() {
        return lastSignInIp;
    }

    public void setLastSignInIp(String lastSignInIp) {
        this.lastSignInIp = lastSignInIp;
    }

    public Integer getSignInCount() {
        return signInCount;
    }

    public void setSignInCount(Integer signInCount) {
        this.signInCount = signInCount;
    }

    public Integer getMachineQuotaLimit() {
        return machineQuotaLimit;
    }

    public void setAffiliation(EmployerDto affiliation) {
        this.affiliation = affiliation;
    }

    public void setMachineQuotaLimit(Integer machineQuotaLimit) {
        this.machineQuotaLimit = machineQuotaLimit;
    }

    public EmployerDto getAffiliation() {
        return affiliation;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }
}
