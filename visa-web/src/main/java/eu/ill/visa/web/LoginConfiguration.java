package eu.ill.visa.web;

import javax.validation.constraints.NotNull;

public class LoginConfiguration {

    @NotNull
    private String realm;

    @NotNull
    private String url;

    @NotNull
    private String clientId;


    public String getRealm() {
        return realm;
    }

    public String getUrl() {
        return url;
    }

    public String getClientId() {
        return clientId;
    }
}
