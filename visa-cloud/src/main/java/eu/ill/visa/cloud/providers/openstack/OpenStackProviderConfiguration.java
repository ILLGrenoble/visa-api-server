package eu.ill.visa.cloud.providers.openstack;

import java.util.Map;

public class OpenStackProviderConfiguration {

    private String applicationId;
    private String applicationSecret;

    private String computeEndpoint;
    private String imageEndpoint;
    private String identityEndpoint;

    private String addressProvider;

    private String addressProviderUUID;

    public static OpenStackProviderConfiguration buildFromMap(Map<String, String> values) {
        final OpenStackProviderConfiguration configuration = new OpenStackProviderConfiguration();
        configuration.setApplicationId(values.get("applicationId"));
        configuration.setApplicationSecret(values.get("applicationSecret"));
        configuration.setComputeEndpoint(values.get("computeEndpoint"));
        configuration.setImageEndpoint(values.get("imageEndpoint"));
        configuration.setIdentityEndpoint(values.get("identityEndpoint"));
        configuration.setAddressProvider(values.get("addressProvider"));
        configuration.setAddressProviderUUID(values.get("addressProviderUUID"));
        return configuration;
    }

    private void setAddressProviderUUID(String addressProviderUUID) {
        this.addressProviderUUID = addressProviderUUID;
    }

    public String getAddressProviderUUID() {
        return addressProviderUUID;
    }

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

}
