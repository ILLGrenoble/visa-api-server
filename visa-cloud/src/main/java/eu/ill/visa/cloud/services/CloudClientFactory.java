package eu.ill.visa.cloud.services;

import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.http.HttpClient;
import eu.ill.visa.cloud.http.clients.OkHttpClientAdapter;
import eu.ill.visa.cloud.providers.NullProvider;
import eu.ill.visa.cloud.providers.openstack.OpenStackProvider;
import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notNull;

public class CloudClientFactory {

    private static final String NULL = "null";
    private static final String OPENSTACK = "openstack";

    public CloudClient getClient(final String provider, final Map<String, String> parameters, String serverNamePrefix) throws CloudException {
        if (NULL.equals(provider)) {
            return createNullProvider(serverNamePrefix);

        } else if (OPENSTACK.equals(provider)) {
            return createOpenStackProvider(parameters, serverNamePrefix);

        }
        throw new CloudException("Unsupported provider provided: " + provider);
    }

    private CloudClient createNullProvider(@NotNull @Valid String serverNamePrefix) {
        return new CloudClient(new NullProvider(), serverNamePrefix);
    }

    private CloudClient createOpenStackProvider(final Map<String, String> parameters, @NotNull @Valid String serverNamePrefix) {
        notNull(parameters.get("identityEndpoint"), "Identity endpoint must be set");
        notNull(parameters.get("computeEndpoint"), "Compute endpoint must be set");
        notNull(parameters.get("imageEndpoint"), "CloudImage endpoint must be set");
        notNull(parameters.get("applicationId"), "ApplicationId must be set");
        notNull(parameters.get("applicationSecret"), "ApplicationSecret must be set");

        final OpenStackProviderConfiguration configuration = OpenStackProviderConfiguration.buildFromMap(parameters);
        final HttpClient                     httpClient    = new OkHttpClientAdapter();
        return new CloudClient(new OpenStackProvider(httpClient, configuration), serverNamePrefix);
    }
}
