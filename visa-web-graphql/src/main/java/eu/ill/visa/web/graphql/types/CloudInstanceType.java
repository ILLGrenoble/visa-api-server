package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.domain.CloudInstance;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

@Type("CloudInstance")
public class CloudInstanceType {

    private final String id;
    private final String name;
    private final String address;
    private final CloudInstanceFaultType fault;
    private final List<String> securityGroups;

    public CloudInstanceType(final CloudInstance instance) {
        this.id = instance.getId();
        this.name = instance.getName();
        this.address = instance.getAddress();
        this.fault = instance.getFault() == null ? null : new CloudInstanceFaultType(instance.getFault());
        this.securityGroups = instance.getSecurityGroups();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public CloudInstanceFaultType getFault() {
        return fault;
    }

    public List<String> getSecurityGroups() {
        return securityGroups;
    }
}
