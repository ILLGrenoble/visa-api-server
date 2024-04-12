package eu.ill.visa.business;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class NotificationConfiguration {

    private String              adapter;
    private Map<String, String> parameters;
    private Boolean             enabled = false;


    @JsonProperty("adapter")
    @NotNull
    @Valid
    public String getAdapter() {
        return this.adapter;
    }

    @JsonProperty("parameters")
    public Map<String, String> getParameters() {
        return parameters;
    }

    @JsonProperty("enabled")
    public Boolean getEnabled() {
        return this.enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

}

