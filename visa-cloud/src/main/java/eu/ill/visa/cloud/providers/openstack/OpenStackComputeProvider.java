package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudNotFoundException;
import eu.ill.visa.cloud.providers.openstack.http.responses.Server;
import eu.ill.visa.cloud.providers.openstack.http.ComputeEndpointClient;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
            return this.computeEndpointClient.flavors(this.authenticate()).flavors;

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.flavors(this.authenticate(true)).flavors;

        } catch (Exception e) {
            logger.error("Failed to get cloud flavours from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public CloudFlavour flavor(final String id) throws CloudException {
        try {
            return this.computeEndpointClient.flavor(this.authenticate(), id).flavor;

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.flavor(this.authenticate(true), id).flavor;

        } catch (Exception e) {
            logger.warn("Failed to get cloud flavour with id {} from OpenStack: {}", id, e.getMessage());
            return null;
        }
    }

    public List<CloudInstance> instances() throws CloudException {
        try {
            return this.computeEndpointClient.servers(this.authenticate()).servers.stream()
                .map(server -> server.toCloudInstance(this.configuration.getAddressProvider()))
                .toList();

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.servers(this.authenticate(true)).servers.stream()
                .map(server -> server.toCloudInstance(this.configuration.getAddressProvider()))
                .toList();

        } catch (Exception e) {
            logger.warn("Failed to get cloud instances from OpenStack: {}", e.getMessage());
            return null;
        }
    }

    public CloudInstance instance(final String id) throws CloudException {
        try {
            Server server = this.computeEndpointClient.server(this.authenticate(), id).server;
            return server.toCloudInstance(this.configuration.getAddressProvider());

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.server(this.authenticate(true), id).server.toCloudInstance(this.configuration.getAddressProvider());

        } catch (CloudNotFoundException e) {
            return null;

        } catch (Exception e) {
            logger.warn("Failed to get cloud flavour with id {} from OpenStack: {}", id, e.getMessage());
            throw new CloudException("Error in response getting instance from OpenStack: " + e.getMessage());
        }
    }
}
