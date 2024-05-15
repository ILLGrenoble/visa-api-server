package eu.ill.visa.web.graphqlxx.types;

public class OpenStackProviderConfigurationType {
    private String applicationId;
    private String applicationSecret;
    private String computeEndpoint;
    private String imageEndpoint;
    private String networkEndpoint;
    private String identityEndpoint;
    private String addressProvider;
    private String addressProviderUUID;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    public String getComputeEndpoint() {
        return computeEndpoint;
    }

    public void setComputeEndpoint(String computeEndpoint) {
        this.computeEndpoint = computeEndpoint;
    }

    public String getImageEndpoint() {
        return imageEndpoint;
    }

    public void setImageEndpoint(String imageEndpoint) {
        this.imageEndpoint = imageEndpoint;
    }

    public String getNetworkEndpoint() {
        return networkEndpoint;
    }

    public void setNetworkEndpoint(String networkEndpoint) {
        this.networkEndpoint = networkEndpoint;
    }

    public String getIdentityEndpoint() {
        return identityEndpoint;
    }

    public void setIdentityEndpoint(String identityEndpoint) {
        this.identityEndpoint = identityEndpoint;
    }

    public String getAddressProvider() {
        return addressProvider;
    }

    public void setAddressProvider(String addressProvider) {
        this.addressProvider = addressProvider;
    }

    public String getAddressProviderUUID() {
        return addressProviderUUID;
    }

    public void setAddressProviderUUID(String addressProviderUUID) {
        this.addressProviderUUID = addressProviderUUID;
    }
}
