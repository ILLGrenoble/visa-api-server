package eu.ill.visa.cloud.services;

import eu.ill.visa.cloud.CloudConfiguration;
import eu.ill.visa.cloud.ProviderConfiguration;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.providers.NullProvider;
import eu.ill.visa.cloud.providers.openstack.OpenStackProvider;
import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;
import eu.ill.visa.cloud.providers.web.WebProvider;
import eu.ill.visa.cloud.providers.web.WebProviderConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class CloudClientFactory {

    public static final String NULL      = "null";
    public static final String OPENSTACK = "openstack";
    public static final String WEB       = "web";

    /**
     * Create a new client for the given provider
     *
     * @return a new cloud client
     * @throws CloudException if the provider is not found
     */
    public CloudClient getClient(final CloudConfiguration cloudConfiguration) throws CloudException {
        final String provider = cloudConfiguration.providerType();
        final ProviderConfiguration providerConfiguration = provider.equals(CloudClientFactory.NULL) ? null :  this.getProviderConfiguration(cloudConfiguration, provider);
        final String serverNamePrefix = cloudConfiguration.serverNamePrefix();

        return this.getClient(cloudConfiguration, -1L, cloudConfiguration.providerName(), provider, providerConfiguration, serverNamePrefix, true);
    }

    public CloudClient getClient(CloudConfiguration cloudConfiguration, Long id, String name, String provider, ProviderConfiguration providerConfiguration, String serverNamePrefix, boolean visible) throws CloudException {
        if (NULL.equals(provider)) {
            return createNullProvider(id, name, "null", serverNamePrefix, visible);

        } else if (OPENSTACK.equals(provider)) {
            return createOpenStackProvider(cloudConfiguration, id, name, provider, providerConfiguration.parameters(), serverNamePrefix, visible);

        } else if (WEB.equals(provider)) {
            return createWebProvider(cloudConfiguration, id, name, provider, providerConfiguration.parameters(), serverNamePrefix, visible);
        }
        throw new CloudException(format("Unsupported provider provided: %s", provider));
    }

    private CloudClient createNullProvider(@NotNull Long id, @NotNull String name, @NotNull String provider, @NotNull @Valid String serverNamePrefix, boolean visible) {
        return new CloudClient(id, name, provider, new NullProvider(), serverNamePrefix, visible);
    }

    /**
     * This provider enables using openstack
     * This is the officially supported provider for VISA
     * Please use the web provider implementation if you wish to use a different cloud provider
     */
    private CloudClient createOpenStackProvider(@NotNull CloudConfiguration cloudConfiguration, @NotNull Long id, @NotNull String name, @NotNull String provider, final Map<String, String> parameters, @NotNull @Valid String serverNamePrefix, boolean visible) {
        requireNonNull(parameters.get("identityEndpoint"), "identityEndpoint endpoint must be set");
        requireNonNull(parameters.get("computeEndpoint"), "computeEndpoint endpoint must be set");
        requireNonNull(parameters.get("imageEndpoint"), "imageEndpoint endpoint must be set");
        requireNonNull(parameters.get("networkEndpoint"), "networkEndpoint must be set");
        requireNonNull(parameters.get("applicationId"), "applicationId must be set");
        requireNonNull(parameters.get("applicationSecret"), "applicationSecret must be set");
        requireNonNull(parameters.get("addressProvider"), "addressProvider must be set");
        requireNonNull(parameters.get("addressProviderUUID"), "addressProviderUUID must be set");

        final OpenStackProviderConfiguration openStackProviderConfiguration = new OpenStackProviderConfiguration(parameters);
        return new CloudClient(id, name, provider, new OpenStackProvider(cloudConfiguration, openStackProviderConfiguration), serverNamePrefix, visible);
    }

    /**
     * This provider enables using a custom cloud provider i.e. proxmox, vmware etc.
     * It forwards requests to an implementation that encapsulates the underlying cloud provider
     */
    private CloudClient createWebProvider(@NotNull CloudConfiguration cloudConfiguration, @NotNull Long id, @NotNull String name, @NotNull String provider, final Map<String, String> parameters, @NotNull @Valid String serverNamePrefix, boolean visible) {
        final WebProviderConfiguration webProviderConfiguration = new WebProviderConfiguration(
            requireNonNull(parameters.get("url"), "url must be set"),
            requireNonNull(parameters.get("authToken"), "authToken must be set")
        );
        return new CloudClient(id, name, provider, new WebProvider(cloudConfiguration, webProviderConfiguration), serverNamePrefix, visible);
    }


    private ProviderConfiguration getProviderConfiguration(final CloudConfiguration configuration, String provider) {
        Optional<ProviderConfiguration> providerConfiguration = configuration.providers().stream()
            .filter(aProviderConfiguration -> aProviderConfiguration.name().equals(provider))
            .findFirst();
        return providerConfiguration.orElse(null);
    }
}
