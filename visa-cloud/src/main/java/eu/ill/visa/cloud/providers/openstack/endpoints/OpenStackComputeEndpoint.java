package eu.ill.visa.cloud.providers.openstack.endpoints;

import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.domain.CloudInstanceIdentifier;
import eu.ill.visa.cloud.domain.CloudLimit;
import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudNotFoundException;
import eu.ill.visa.cloud.exceptions.CloudClientException;
import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;
import eu.ill.visa.cloud.providers.openstack.http.ComputeEndpointClient;
import eu.ill.visa.cloud.providers.openstack.http.requests.*;
import eu.ill.visa.cloud.providers.openstack.http.responses.SecurityGroupsResponse;
import eu.ill.visa.cloud.providers.openstack.http.responses.Server;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class OpenStackComputeEndpoint implements ComputeEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackComputeEndpoint.class);

    private final OpenStackIdentityEndpoint identityProvider;
    private final ComputeEndpointClient computeEndpointClient;
    private final OpenStackProviderConfiguration configuration;

    private OpenStackComputeEndpoint(final OpenStackProviderConfiguration configuration,
                                    final OpenStackIdentityEndpoint identityProvider) {

        this.identityProvider = identityProvider;
        this.configuration = configuration;
        this.computeEndpointClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.getComputeEndpoint()))
            .build(ComputeEndpointClient.class);
    }

    public static ComputeEndpoint authenticationProxy(final OpenStackProviderConfiguration configuration,
                                                      final OpenStackIdentityEndpoint identityEndpoint) {
        final OpenStackComputeEndpoint openStackComputeEndpoint = new OpenStackComputeEndpoint(configuration, identityEndpoint);
        return (ComputeEndpoint) Proxy.newProxyInstance(
            openStackComputeEndpoint.getClass().getClassLoader(),
            new Class[] { ComputeEndpoint.class }, (target, method, methodArgs) -> {
                try {
                    return method.invoke(openStackComputeEndpoint, methodArgs);

                } catch (CloudAuthenticationException e) {
                    identityEndpoint.authenticate(true);

                    try {
                        return method.invoke(openStackComputeEndpoint, methodArgs);

                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }

                } catch (InvocationTargetException ex) {
                    throw ex.getCause();
                }
            });
    }

    public List<CloudFlavour> flavors() throws CloudException{
        try {
            return this.computeEndpointClient.flavors(this.identityProvider.authenticate()).flavors();

        } catch (CloudClientException e) {
            logger.error("Failed to get cloud flavours from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public CloudFlavour flavor(final String id) throws CloudException {
        try {
            return this.computeEndpointClient.flavor(this.identityProvider.authenticate(), id).flavor();

        } catch (CloudClientException e) {
            logger.warn("Failed to get cloud flavour with id {} from OpenStack: {}", id, e.getMessage());
            return null;
        }
    }

    public List<CloudInstance> instances() throws CloudException {
        try {
            return this.computeEndpointClient.servers(this.identityProvider.authenticate()).servers().stream()
                .map(server -> server.toCloudInstance(this.configuration.getAddressProvider()))
                .toList();

        } catch (CloudClientException e) {
            logger.warn("Failed to get cloud instances from OpenStack: {}", e.getMessage());
            return null;
        }
    }

    public CloudInstance instance(final String id) throws CloudException {
        try {
            Server server = this.computeEndpointClient.server(this.identityProvider.authenticate(), id).server();
            return server.toCloudInstance(this.configuration.getAddressProvider());

        } catch (CloudNotFoundException e) {
            return null;

        } catch (CloudClientException e) {
            throw new CloudException("Error in response getting instance from OpenStack: " + e.getMessage());
        }
    }

    public List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException {
        try {
            return this.computeEndpointClient.servers(this.identityProvider.authenticate()).servers().stream()
                .map(server -> server.toCloudInstanceIdentifier(this.configuration.getAddressProvider()))
                .toList();

        } catch (CloudClientException e) {
            logger.warn("Failed to get cloud instance identifier from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public void rebootInstance(final String id) throws CloudException {
        this.runServerAction(id, new RebootInstanceActionRequest(), "Could not reboot server");
    }

    public void startInstance(final String id) throws CloudException {
        this.runServerAction(id, new StartInstanceActionRequest(), "Could not start server");
    }

    public void shutdownInstance(final String id) throws CloudException {
        this.runServerAction(id, new StopInstanceActionRequest(), "Could not shutdown server");
    }

    public void addServerSecurityGroup(final String id, final String securityGroup) throws CloudException {
        this.runServerAction(id, new AddSecurityGroupInstanceActionRequest(securityGroup), "Could not add server security group");
    }

    public void removeServerSecurityGroup(final String id, final String securityGroup) throws CloudException {
        this.runServerAction(id, new RemoveSecurityGroupInstanceActionRequest(securityGroup), "Could not remove server security group");
    }

    public void deleteInstance(final String id) throws CloudException {
        try {
            this.computeEndpointClient.deleteServer(this.identityProvider.authenticate(), id);

        } catch (CloudClientException e) {
            throw new CloudException(format("Could not delete server with id %s and response %s: ", id, e.getMessage()));
        }
    }

    public String createInstance(final ServerInput input) throws CloudException {
        try {
            return this.computeEndpointClient.createServer(this.identityProvider.authenticate(), new ServerRequest(input)).server().id();

        } catch (CloudClientException e) {
            throw new CloudException(format("Could not create server with name %s and response %s ", input.name, e.getMessage()));
        }
    }

    public List<String> serverSecurityGroups(final String id) throws CloudException {
        try {
            return this.computeEndpointClient.serverSecurityGroups(this.identityProvider.authenticate(), id).securityGroups().stream()
                .map(SecurityGroupsResponse.SecurityGroup::name)
                .toList();

        } catch (CloudClientException e) {
            logger.error("Failed to get security groups for server with id {} from OpenStack: {}", id, e.getMessage());
            return new ArrayList<>();
        }
    }

    public CloudLimit limits() throws CloudException {
        try {
            return this.computeEndpointClient.limits(this.identityProvider.authenticate()).limits().absolute();

        } catch (CloudClientException e) {
            logger.warn("Failed to get cloud limits from OpenStack: {}", e.getMessage());
            return null;
        }
    }

    private void runServerAction(final String id, final InstanceActionRequest action, final String errorMessage) throws CloudException {
        try {
            this.computeEndpointClient.runServerAction(this.identityProvider.authenticate(), id, action);

        } catch (CloudClientException e) {
            throw new CloudException(format("%s for server with id %s: %s: ", errorMessage, id, e.getMessage()));
        }
    }


}
