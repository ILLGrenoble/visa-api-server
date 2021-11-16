package eu.ill.visa.web;

import javax.validation.constraints.NotNull;

public class LoginConfiguration {

    @NotNull
    private String scope;

    @NotNull
    private boolean showDebugInformation;

    @NotNull
    private boolean sessionChecksEnabled;

    @NotNull
    private String issuer;

    @NotNull
    private String clientId;

    public String getIssuer() {
        return issuer;
    }

    public String getClientId() {
        return clientId;
    }

    public String getScope() {
        return scope;
    }

    public boolean isShowDebugInformation() {
        return showDebugInformation;
    }

    public boolean isSessionChecksEnabled() {
        return sessionChecksEnabled;
    }
}
