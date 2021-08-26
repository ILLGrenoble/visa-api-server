package eu.ill.visa.cloud.services;

import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.http.HttpClient;
import eu.ill.visa.cloud.http.clients.OkHttpClientAdapter;
import eu.ill.visa.cloud.providers.NullProvider;
import eu.ill.visa.cloud.providers.openstack.OpenStackProvider;
import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;
import eu.ill.visa.cloud.providers.web.WebProvider;
import eu.ill.visa.cloud.providers.web.WebProviderConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class CloudClientFactory {

    private static final String NULL      = "null";
    private static final String OPENSTACK = "openstack";
    private static final String WEB       = "web";

    /**
     * Create a new client for the given provider
     *
     * @param provider         the cloud provider to be used
     * @param parameters       the configuration parameters
     * @param serverNamePrefix the server name prefix
     * @return a new cloud client
     * @throws CloudException if the provider is not found
     */
    public CloudClient getClient(final String provider,
                                 final Map<String, String> parameters,
                                 final String serverNamePrefix) throws CloudException {
        if (NULL.equals(provider)) {
            return createNullProvider(serverNamePrefix);
        } else if (OPENSTACK.equals(provider)) {
            return createOpenStackProvider(parameters, serverNamePrefix);
        } else if (WEB.equals(provider)) {
            return createWebProvider(parameters, serverNamePrefix);
        }
        throw new CloudException(format("Unsupported provider provided: %s", provider));
    }

    private CloudClient createNullProvider(@NotNull @Valid String serverNamePrefix) {
        return new CloudClient(new NullProvider(), serverNamePrefix);
    }

    /**
     * This provider enables using openstack
     * This is the officially supported provider for VISA
     * Please use the web provider implementation if you wish to use a different cloud provider
     */
    private CloudClient createOpenStackProvider(final Map<String, String> parameters, @NotNull @Valid String serverNamePrefix) {
        requireNonNull(parameters.get("identityEndpoint"), "identityEndpoint endpoint must be set");
        requireNonNull(parameters.get("computeEndpoint"), "computeEndpoint endpoint must be set");
        requireNonNull(parameters.get("imageEndpoint"), "imageEndpoint endpoint must be set");
        requireNonNull(parameters.get("networkEndpoint"), "networkEndpoint must be set");
        requireNonNull(parameters.get("applicationId"), "applicationId must be set");
        requireNonNull(parameters.get("applicationSecret"), "applicationSecret must be set");
        requireNonNull(parameters.get("addressProvider"), "addressProvider must be set");
        requireNonNull(parameters.get("addressProviderUUID"), "addressProviderUUID must be set");

        final OpenStackProviderConfiguration configuration = new OpenStackProviderConfiguration(parameters);
        final HttpClient httpClient = new OkHttpClientAdapter();
        return new CloudClient(new OpenStackProvider(httpClient, configuration), serverNamePrefix);
    }

    /**
     * This provider enables using a custom cloud provider i.e. proxmox, vmware etc.
     * It forwards requests to an implementation that encapsulates the underlying cloud provider
     */
    private CloudClient createWebProvider(final Map<String, String> parameters, @NotNull @Valid String serverNamePrefix) {
        final WebProviderConfiguration configuration = new WebProviderConfiguration(
            requireNonNull(parameters.get("url"), "url must be set"),
            requireNonNull(parameters.get("authToken"), "authToken must be set")
        );
        final HttpClient httpClient = new OkHttpClientAdapter();
        return new CloudClient(new WebProvider(httpClient, configuration), serverNamePrefix);
    }

}
