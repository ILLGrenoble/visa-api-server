package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.domain.CloudFlavour;
import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.providers.openstack.http.ComputeEndpointClient;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OpenStackComputeProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackComputeProvider.class);

    private final ComputeEndpointClient computeEndpointClient;
    private final OpenStackIdentityProvider identityProvider;

    public OpenStackComputeProvider(final OpenStackProviderConfiguration configuration,
                                    final OpenStackIdentityProvider identityProvider) {
        this.computeEndpointClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.getComputeEndpoint()))
            .build(ComputeEndpointClient.class);
        this.identityProvider = identityProvider;
    }

    public List<CloudFlavour> flavors() throws CloudException {
        try {
            return this.computeEndpointClient.flavors(this.identityProvider.authenticate()).flavors;

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.flavors(this.identityProvider.authenticate(true)).flavors;

        } catch (Exception e) {
            logger.error("Failed to get cloud flavours from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public CloudFlavour flavor(final String id) throws CloudException {
        try {
            return this.computeEndpointClient.flavor(this.identityProvider.authenticate(), id).flavor;

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.computeEndpointClient.flavor(this.identityProvider.authenticate(true), id).flavor;

        } catch (Exception e) {
            logger.warn("Failed to get cloud flavour with id {} from OpenStack: {}", id, e.getMessage());
            return null;
        }
    }
}
