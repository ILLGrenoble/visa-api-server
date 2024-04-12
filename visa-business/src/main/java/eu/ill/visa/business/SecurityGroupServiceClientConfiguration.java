package eu.ill.visa.business;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "business.securityGroupServiceClient")
public class SecurityGroupServiceClientConfiguration {

    private Boolean enabled = false;
    private String url;
    private String authToken;

    @JsonProperty("enabled")
    public Boolean getEnabled() {
        return this.enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    @JsonProperty("url")
    public String getUrl() {
        return this.url;
    }

    @JsonProperty("authToken")
    public String getAuthToken() {
        return this.authToken;
    }

}

