package eu.ill.visa.cloud.providers.openstack;

import java.util.Map;

public class OpenStackProviderConfiguration {

    private final String applicationId;
    private final String applicationSecret;

    private final String computeEndpoint;
    private final String placementEndpoint;
    private final String imageEndpoint;
    private final String identityEndpoint;
    private final String networkEndpoint;

    private final String addressProvider;

    private final String addressProviderUUID;

    public OpenStackProviderConfiguration(final Map<String, String> values) {
        this.applicationId = this.getValidParameterValue(values, "applicationId");
        this.applicationSecret = this.getValidParameterValue(values, "applicationSecret");
        this.computeEndpoint = this.getValidParameterValue(values, "computeEndpoint");
        this.placementEndpoint = this.getValidParameterValue(values, "placementEndpoint");
        this.imageEndpoint = this.getValidParameterValue(values, "imageEndpoint");
        this.identityEndpoint = this.getValidParameterValue(values, "identityEndpoint");
        this.networkEndpoint = this.getValidParameterValue(values, "networkEndpoint");
        this.addressProvider = this.getValidParameterValue(values, "addressProvider");
        this.addressProviderUUID = this.getValidParameterValue(values, "addressProviderUUID");
    }

    private String getValidParameterValue(final Map<String, String> parameters, final String parameterName) {
        if (parameters.containsKey(parameterName)) {
            final String parameterValue = parameters.get(parameterName);
            if (parameterValue.equals("null")) {
                return null;
            } else if (parameterValue.isEmpty()) {
                return null;
            } else {
                return parameterValue;
            }
        }
        return null;
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

    public String getPlacementEndpoint() {
        return placementEndpoint;
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
