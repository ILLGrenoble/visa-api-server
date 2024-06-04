package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.providers.openstack.http.ImageEndpointClient;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OpenStackImageProvider extends AuthenticatedOpenStackProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackImageProvider.class);

    private final ImageEndpointClient imageEndpointClient;

    public OpenStackImageProvider(final OpenStackProviderConfiguration configuration,
                                  final OpenStackIdentityProvider identityProvider) {
        super(identityProvider);
        this.imageEndpointClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.getImageEndpoint()))
            .build(ImageEndpointClient.class);
    }

    public List<CloudImage> images() throws CloudException {
        try {
            return this.imageEndpointClient.images(this.authenticate()).images();

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.imageEndpointClient.images(this.authenticate(true)).images();

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            logger.error("Failed to get cloud images from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public CloudImage image(final String id) throws CloudException {
        try {
            return this.imageEndpointClient.image(this.authenticate(), id);

        } catch (CloudAuthenticationException e) {
            // Force creation of new authentication token
            return this.imageEndpointClient.image(this.authenticate(true), id);

        } catch (CloudException e) {
            throw e;

        } catch (Exception e) {
            logger.warn("Failed to get cloud image with id {} from OpenStack: {}", id, e.getMessage());
            return null;
        }
    }
}