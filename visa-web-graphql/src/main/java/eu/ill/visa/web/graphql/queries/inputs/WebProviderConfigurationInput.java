package eu.ill.visa.web.graphql.queries.inputs;

import jakarta.validation.constraints.NotNull;

public class WebProviderConfigurationInput {

    @NotNull
    private String url;

    @NotNull
    private String authToken;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
