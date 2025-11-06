package eu.ill.visa.web.graphql.inputs;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Input;

@Input("OpenStackProviderConfigurationInput")
public class OpenStackProviderConfigurationInput {

    private @NotNull String applicationId;
    private @NotNull String applicationSecret;
    private @NotNull String computeEndpoint;
    private String placementEndpoint; // Added to optionally manage PCI devices (eg PCI_PASSTHROUGH GPUs)
    private @NotNull String imageEndpoint;
    private @NotNull String networkEndpoint;
    private @NotNull String identityEndpoint;
    private @NotNull String addressProvider;
    private @NotNull String addressProviderUUID;

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

    public String getPlacementEndpoint() {
        return placementEndpoint;
    }

    public void setPlacementEndpoint(String placementEndpoint) {
        this.placementEndpoint = placementEndpoint;
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
