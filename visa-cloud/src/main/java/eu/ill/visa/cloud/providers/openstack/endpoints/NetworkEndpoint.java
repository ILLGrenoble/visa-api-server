package eu.ill.visa.cloud.providers.openstack.endpoints;

import eu.ill.visa.cloud.exceptions.CloudException;

import java.util.List;

public interface NetworkEndpoint {

    List<String> securityGroups() throws CloudException;
}
