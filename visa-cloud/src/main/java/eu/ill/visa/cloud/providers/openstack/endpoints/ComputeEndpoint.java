package eu.ill.visa.cloud.providers.openstack.endpoints;

import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.providers.openstack.http.requests.ServerInput;

import java.util.List;

public interface ComputeEndpoint {
    List<CloudFlavour> flavors() throws CloudException;
    CloudFlavour flavor(final String id) throws CloudException;
    List<CloudDevice> devices() throws CloudException;
    List<CloudDeviceAllocation> flavorDeviceAllocations(String flavourId) throws CloudException;
    List<CloudInstance> instances() throws CloudException;
    CloudInstance instance(final String id) throws CloudException;
    List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException;
    void rebootInstance(final String id) throws CloudException;
    void startInstance(final String id) throws CloudException;
    void shutdownInstance(final String id) throws CloudException;
    void addServerSecurityGroup(final String id, final String securityGroup) throws CloudException;
    void removeServerSecurityGroup(final String id, final String securityGroup) throws CloudException;
    void deleteInstance(final String id) throws CloudException;
    String createInstance(final ServerInput input) throws CloudException;
    List<String> serverSecurityGroups(final String id) throws CloudException;
    CloudLimit limits() throws CloudException;
}
