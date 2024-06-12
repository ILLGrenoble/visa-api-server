package eu.ill.visa.cloud.providers.openstack.endpoints;

import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudAuthenticationException;
import eu.ill.visa.cloud.exceptions.CloudClientException;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;
import eu.ill.visa.cloud.providers.openstack.http.ImageEndpointClient;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class OpenStackImageEndpoint implements ImageEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackImageEndpoint.class);

    private final OpenStackIdentityEndpoint identityProvider;
    private final ImageEndpointClient imageEndpointClient;

    private OpenStackImageEndpoint(final OpenStackProviderConfiguration configuration,
                                  final OpenStackIdentityEndpoint identityProvider) {
        this.identityProvider = identityProvider;
        this.imageEndpointClient = QuarkusRestClientBuilder.newBuilder()
            .baseUri(URI.create(configuration.getImageEndpoint()))
            .build(ImageEndpointClient.class);
    }

    public static ImageEndpoint authenticationProxy(final OpenStackProviderConfiguration configuration,
                                                      final OpenStackIdentityEndpoint identityEndpoint) {
        final OpenStackImageEndpoint openStackImageEndpoint = new OpenStackImageEndpoint(configuration, identityEndpoint);
        return (ImageEndpoint) Proxy.newProxyInstance(
            openStackImageEndpoint.getClass().getClassLoader(),
            new Class[] { ImageEndpoint.class }, (target, method, methodArgs) -> {
                try {
                    return method.invoke(openStackImageEndpoint, methodArgs);

                } catch (CloudAuthenticationException e) {
                    identityEndpoint.authenticate(true);

                    try {
                        return method.invoke(openStackImageEndpoint, methodArgs);

                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }

                } catch (InvocationTargetException ex) {
                    throw ex.getCause();
                }
            });
    }

    public List<CloudImage> images() throws CloudException {
        try {
            return this.imageEndpointClient.images(this.identityProvider.authenticate()).images();

        } catch (CloudClientException e) {
            logger.error("Failed to get cloud images from OpenStack: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public CloudImage image(final String id) throws CloudException {
        try {
            return this.imageEndpointClient.image(this.identityProvider.authenticate(), id);

        } catch (CloudClientException e) {
            logger.warn("Failed to get cloud image with id {} from OpenStack: {}", id, e.getMessage());
            return null;
        }
    }
}