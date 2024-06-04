package eu.ill.visa.cloud.providers.openstack.http.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SecurityGroupsResponse(@JsonProperty("security_groups") List<SecurityGroup> securityGroups) {
    public record SecurityGroup(String name) {
    }
}
