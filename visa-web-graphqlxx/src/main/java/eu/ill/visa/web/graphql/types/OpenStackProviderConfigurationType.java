package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;

public class OpenStackProviderConfigurationType {
    private final String applicationId;
    private final String applicationSecret;
    private final String computeEndpoint;
    private final String imageEndpoint;
    private final String networkEndpoint;
    private final String identityEndpoint;
    private final String addressProvider;
    private final String addressProviderUUID;

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
