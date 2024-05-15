package eu.ill.visa.web.graphqlxx.types;

import java.util.List;

public class CloudInstanceType {

    private String id;
    private String name;
    private String address;
    private CloudInstanceFaultType fault;
    private List<String> securityGroups;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public CloudInstanceFaultType getFault() {
        return fault;
    }

    public void setFault(CloudInstanceFaultType fault) {
        this.fault = fault;
    }

    public List<String> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<String> securityGroups) {
        this.securityGroups = securityGroups;
    }
}
