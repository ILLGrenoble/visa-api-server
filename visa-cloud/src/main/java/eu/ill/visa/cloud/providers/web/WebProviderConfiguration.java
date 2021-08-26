package eu.ill.visa.cloud.providers.web;

public class WebProviderConfiguration {

    private final String url;

    private final String authToken;

    public WebProviderConfiguration(final String url,
                                    final String authToken) {
        this.url = url;
        this.authToken = authToken;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthToken() {
        return authToken;
    }

}
