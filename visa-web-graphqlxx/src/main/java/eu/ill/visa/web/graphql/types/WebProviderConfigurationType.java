package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.providers.web.WebProviderConfiguration;

public class WebProviderConfigurationType {
    private final String url;
    private final String authToken;

    public WebProviderConfigurationType(final WebProviderConfiguration configuration) {
        this.url = configuration.getUrl();
        this.authToken = configuration.getAuthToken();
    }

    public String getUrl() {
        return url;
    }

    public String getAuthToken() {
        return authToken;
    }
}
