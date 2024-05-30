package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.providers.web.WebProviderConfiguration;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("WebProviderConfiguration")
public class WebProviderConfigurationType {

    private final @NotNull String url;
    private final @NotNull String authToken;

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
