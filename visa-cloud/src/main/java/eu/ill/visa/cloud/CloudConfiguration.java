package eu.ill.visa.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public class CloudConfiguration {

    private String provider;

    private List<ProviderConfiguration> providers;

    private String serverNamePrefix;

    public CloudConfiguration() {
    }

    public CloudConfiguration(String provider) {
        this.provider = provider;
    }

    @JsonProperty
    public String getProvider() {
        return provider;
    }

    @JsonProperty
    public void setProvider(String provider) {
        this.provider = provider;
    }

    public ProviderConfiguration getProviderConfiguration(String provider) {
        Optional<ProviderConfiguration> configuration = providers.stream()
            .filter(providerConfiguration -> providerConfiguration.getName().equals(provider))
            .findFirst();
        return configuration.orElse(null);
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
}
