package eu.ill.visa.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class CloudConfiguration {

    private String provider;

    private Map<String, String> parameters;

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

    @JsonProperty
    public Map<String, String> getParameters() {
        return parameters;
    }

    @JsonProperty
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @JsonProperty
    @NotNull
    @Valid
    public String getServerNamePrefix() {
        return serverNamePrefix;
    }
}
