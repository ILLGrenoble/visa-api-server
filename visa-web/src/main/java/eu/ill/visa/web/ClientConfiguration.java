package eu.ill.visa.web;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ClientConfiguration {

    @NotNull
    @Valid
    private LoginConfiguration loginConfiguration;

    @NotNull
    @Valid
    private AnalyticsConfiguration analyticsConfiguration;

    @NotNull
    @Valid
    private DesktopConfiguration desktopConfiguration;

    private String contactEmail;

    @JsonProperty("login")
    public LoginConfiguration getLoginConfiguration() {
        return loginConfiguration;
    }

    public void setLoginConfiguration(LoginConfiguration loginConfiguration) {
        this.loginConfiguration = loginConfiguration;
    }

    @JsonProperty("analytics")
    public AnalyticsConfiguration getAnalyticsConfiguration() {
        return analyticsConfiguration;
    }

    public void setAnalyticsConfiguration(AnalyticsConfiguration analyticsConfiguration) {
        this.analyticsConfiguration = analyticsConfiguration;
    }

    @JsonProperty("desktop")
    public DesktopConfiguration getDesktopConfiguration() {
        return desktopConfiguration;
    }

    @JsonProperty("contactEmail")
    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

}
