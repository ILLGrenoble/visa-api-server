package eu.ill.visa.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ProviderConfiguration {
    private String name;
    private Map<String, String> parameters;

    @JsonProperty
    public Map<String, String> getParameters() {
        return parameters;
    }

    @JsonProperty
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
