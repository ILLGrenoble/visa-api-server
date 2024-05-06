package eu.ill.visa.web.graphql.queries.inputs;

import jakarta.validation.constraints.NotNull;

public class OpenStackProviderConfigurationInput {

    @NotNull
    private String applicationId;

    @NotNull
    private String applicationSecret;

    @NotNull
    private String computeEndpoint;

    @NotNull
    private String imageEndpoint;

    @NotNull
    private String networkEndpoint;

    @NotNull
    private String identityEndpoint;

    @NotNull
    private String addressProvider;

    @NotNull
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
