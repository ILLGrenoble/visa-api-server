package eu.ill.visa.cloud.providers.openstack;

import java.util.Map;

public class OpenStackProviderConfiguration {

    private final String applicationId;
    private final String applicationSecret;

    private final String computeEndpoint;
    private final String imageEndpoint;
    private final String identityEndpoint;
    private final String networkEndpoint;

    private final String addressProvider;

    private final String addressProviderUUID;

    public OpenStackProviderConfiguration(final Map<String, String> values) {
        this.applicationId = values.get("applicationId");
        this.applicationSecret = values.get("applicationSecret");
        this.computeEndpoint = values.get("computeEndpoint");
        this.imageEndpoint = values.get("imageEndpoint");
        this.networkEndpoint = values.get("networkEndpoint");
        this.identityEndpoint = values.get("identityEndpoint");
        this.addressProvider = values.get("addressProvider");
        this.addressProviderUUID = values.get("addressProviderUUID");
    }

    public String getAddressProviderUUID() {
        return addressProviderUUID;
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

    public String getIdentityEndpoint() {
        return identityEndpoint;
    }

    public String getAddressProvider() {
        return addressProvider;
    }

    public String getNetworkEndpoint() {
        return networkEndpoint;
    }

}
