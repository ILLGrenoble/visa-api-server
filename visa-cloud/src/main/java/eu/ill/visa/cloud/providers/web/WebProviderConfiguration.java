package eu.ill.visa.cloud.providers.web;

public class WebProviderConfiguration {

    private final String url;

    private final String authToken;

    public WebProviderConfiguration(final String url,
                                    final String authToken) {
        this.url = url.equals("null") ? null : url;
        this.authToken = authToken.equals("null") ? null : authToken;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthToken() {
        return authToken;
    }

}
