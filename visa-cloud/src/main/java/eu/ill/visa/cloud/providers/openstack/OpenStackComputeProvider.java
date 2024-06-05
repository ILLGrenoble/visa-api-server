package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.domain.CloudInstanceIdentifier;
import eu.ill.visa.cloud.domain.CloudLimit;
import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudNotFoundException;
import eu.ill.visa.cloud.providers.openstack.http.requests.*;
import eu.ill.visa.cloud.providers.openstack.http.responses.SecurityGroupsResponse;
import eu.ill.visa.cloud.providers.openstack.http.responses.Server;
import eu.ill.visa.cloud.providers.openstack.http.ComputeEndpointClient;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class OpenStackComputeProvider extends AuthenticatedOpenStackProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackComputeProvider.class);

    private final ComputeEndpointClient computeEndpointClient;
    private final OpenStackProviderConfiguration configuration;

    public OpenStackComputeProvider(final OpenStackProviderConfiguration configuration,
                                    final OpenStackIdentityProvider identityProvider) {
        super(identityProvider);
        this.configuration = configuration;
        this.computeEndpointClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.getComputeEndpoint()))
            .build(ComputeEndpointClient.class);
    }

    public List<CloudFlavour> flavors() throws CloudException {
        try {
            return this.computeEndpointClient.flavors(this.authenticate()).flavors();

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.flavors(this.authenticate(true)).flavors();

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            logger.error("Failed to get cloud flavours from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public CloudFlavour flavor(final String id) throws CloudException {
        try {
            return this.computeEndpointClient.flavor(this.authenticate(), id).flavor();

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.flavor(this.authenticate(true), id).flavor();

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            logger.warn("Failed to get cloud flavour with id {} from OpenStack: {}", id, e.getMessage());
            return null;
        }
    }

    public List<CloudInstance> instances() throws CloudException {
        try {
            return this.computeEndpointClient.servers(this.authenticate()).servers().stream()
                .map(server -> server.toCloudInstance(this.configuration.getAddressProvider()))
                .toList();

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.servers(this.authenticate(true)).servers().stream()
                .map(server -> server.toCloudInstance(this.configuration.getAddressProvider()))
                .toList();

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            logger.warn("Failed to get cloud instances from OpenStack: {}", e.getMessage());
            return null;
        }
    }

    public CloudInstance instance(final String id) throws CloudException {
        try {
            Server server = this.computeEndpointClient.server(this.authenticate(), id).server();
            return server.toCloudInstance(this.configuration.getAddressProvider());

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.server(this.authenticate(true), id).server().toCloudInstance(this.configuration.getAddressProvider());

        } catch (CloudNotFoundException e) {
            return null;

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            throw new CloudException("Error in response getting instance from OpenStack: " + e.getMessage());
        }
    }

    public List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException {
        try {
            return this.computeEndpointClient.servers(this.authenticate()).servers().stream()
                .map(server -> server.toCloudInstanceIdentifier(this.configuration.getAddressProvider()))
                .toList();

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.servers(this.authenticate(true)).servers().stream()
                .map(server -> server.toCloudInstanceIdentifier(this.configuration.getAddressProvider()))
                .toList();

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            logger.warn("Failed to get cloud instance identifier from OpenStack: {}", e.getMessage());
            return null;
        }
    }

    public void rebootInstance(final String id) throws CloudException {
        this.runServerAction(id, new StartInstanceActionRequest(), "Could not reboot server");
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
            this.computeEndpointClient.deleteServer(this.authenticate(), id);

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            this.computeEndpointClient.deleteServer(this.authenticate(true), id);

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            throw new CloudException(format("Could not delete server with id %s and response %s: ", id, e.getMessage()));
        }
    }

    public String createInstance(final ServerInput input) throws CloudException {
        try {
            return this.computeEndpointClient.createServer(this.authenticate(), new ServerRequest(input)).server().id();

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.createServer(this.authenticate(true), new ServerRequest(input)).server().id();

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            throw new CloudException(format("Could not create server with name %s and response %s ", input.name, e.getMessage()));
        }
    }

    public List<String> serverSecurityGroups(final String id) throws CloudException {
        try {
            return this.computeEndpointClient.serverSecurityGroups(this.authenticate(), id).securityGroups().stream()
                .map(SecurityGroupsResponse.SecurityGroup::name)
                .toList();

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.serverSecurityGroups(this.authenticate(true), id).securityGroups().stream()
                .map(SecurityGroupsResponse.SecurityGroup::name)
                .toList();

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            logger.error("Failed to get security groups for server with id {} from OpenStack: {}", id, e.getMessage());
            return new ArrayList<>();
        }
    }

    private void runServerAction(final String id, final InstanceActionRequest action, final String errorMessage) throws CloudException {
        try {
            this.computeEndpointClient.runServerAction(this.authenticate(), id, action);

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            this.computeEndpointClient.runServerAction(this.authenticate(true), id, action);

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            throw new CloudException(format("%s for server with id %s: %s: ", errorMessage, id, e.getMessage()));
        }
    }


    public CloudLimit limits() throws CloudException {
        try {
            return this.computeEndpointClient.limits(this.authenticate()).limits().absolute();

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.limits(this.authenticate(true)).limits().absolute();

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            logger.warn("Failed to get cloud limits from OpenStack: {}", e.getMessage());
            return null;
        }
    }

}
