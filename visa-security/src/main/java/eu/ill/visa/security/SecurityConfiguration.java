package eu.ill.visa.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ill.visa.security.configuration.TokenConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SecurityConfiguration {

    @NotNull
    @Valid
    private TokenConfiguration tokenConfiguration;


    @JsonProperty("token")
    public TokenConfiguration getTokenConfiguration() {
        return tokenConfiguration;
    }

}
