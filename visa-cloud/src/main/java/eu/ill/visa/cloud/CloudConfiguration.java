package eu.ill.visa.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public class CloudConfiguration {

    private String providerType;

    private String providerName = "Default";

    private List<ProviderConfiguration> providers;

    private String serverNamePrefix;

    public CloudConfiguration() {
    }

    public CloudConfiguration(String providerType) {
        this.providerType = providerType;
    }

    @JsonProperty
    @NotNull
    @Valid
    public String getProviderType() {
        return providerType;
    }

    @JsonProperty
    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    @JsonProperty
    @NotNull
    @Valid
    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    @JsonProperty
    @NotNull
    @Valid
    public String getServerNamePrefix() {
        return serverNamePrefix;
    }

    @JsonProperty
    public List<ProviderConfiguration> getProviders() {
        return providers;
    }

    @JsonProperty
    public void setProviders(List<ProviderConfiguration> providers) {
        this.providers = providers;
    }

    public ProviderConfiguration getProviderConfiguration(String provider) {
        Optional<ProviderConfiguration> configuration = providers.stream()
            .filter(providerConfiguration -> providerConfiguration.getName().equals(provider))
            .findFirst();
        return configuration.orElse(null);
    }
}
