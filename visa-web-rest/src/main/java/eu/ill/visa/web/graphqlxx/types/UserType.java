package eu.ill.visa.web.graphqlxx.types;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserType {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String fullName;
    private EmployerType affiliation;
    private List<ExperimentType> experiments = new ArrayList<>();
    private Integer instanceQuota;
    private final List<UserRoleType> activeUserRoles = new ArrayList<>();
    private final List<RoleType> groups = new ArrayList<>();
    private Date lastSeenAt;
    private Date activatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public EmployerType getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(EmployerType affiliation) {
        this.affiliation = affiliation;
    }

    public List<ExperimentType> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<ExperimentType> experiments) {
        this.experiments = experiments;
    }

    public Integer getInstanceQuota() {
        return instanceQuota;
    }

    public void setInstanceQuota(Integer instanceQuota) {
        this.instanceQuota = instanceQuota;
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

    public void setLastSeenAt(Date lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Date getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Date activatedAt) {
        this.activatedAt = activatedAt;
    }
}
