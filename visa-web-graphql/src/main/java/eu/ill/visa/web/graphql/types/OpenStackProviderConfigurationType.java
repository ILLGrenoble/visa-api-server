package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("OpenStackProviderConfiguration")
public class OpenStackProviderConfigurationType {
    private final @NotNull String applicationId;
    private final @NotNull String applicationSecret;
    private final @NotNull String computeEndpoint;
    private final @NotNull String imageEndpoint;
    private final @NotNull String networkEndpoint;
    private final @NotNull String identityEndpoint;
    private final @NotNull String addressProvider;
    private final @NotNull String addressProviderUUID;

    public OpenStackProviderConfigurationType(final OpenStackProviderConfiguration configuration) {
        this.applicationId = configuration.getApplicationId();
        this.applicationSecret = configuration.getApplicationSecret();
        this.computeEndpoint = configuration.getComputeEndpoint();
        this.imageEndpoint = configuration.getImageEndpoint();
        this.networkEndpoint = configuration.getNetworkEndpoint();
        this.identityEndpoint = configuration.getIdentityEndpoint();
        this.addressProvider = configuration.getAddressProvider();
        this.addressProviderUUID = configuration.getAddressProviderUUID();
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public String getComputeEndpoint() {
        return computeEndpoint;
    }

    public String getImageEndpoint() {
        return imageEndpoint;
    }

    public String getNetworkEndpoint() {
        return networkEndpoint;
    }

    public String getIdentityEndpoint() {
        return identityEndpoint;
    }

    public String getAddressProvider() {
        return addressProvider;
    }

    public String getAddressProviderUUID() {
        return addressProviderUUID;
    }
}
