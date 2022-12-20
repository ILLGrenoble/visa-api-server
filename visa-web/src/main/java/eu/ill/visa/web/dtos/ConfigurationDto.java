package eu.ill.visa.web.dtos;

import eu.ill.visa.web.AnalyticsConfiguration;
import eu.ill.visa.web.DesktopConfiguration;
import eu.ill.visa.web.ExperimentsConfiguration;
import eu.ill.visa.web.LoginConfiguration;

import java.util.Map;

public class ConfigurationDto {

    private String version;
    private LoginConfiguration login;
    private AnalyticsConfiguration analytics;
    private DesktopConfiguration desktop;
    private ExperimentsConfiguration experiments;
    private Map<String, String> metadata;

    private String contactEmail;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LoginConfiguration getLogin() {
        return login;
    }

    public void setLogin(LoginConfiguration login) {
        this.login = login;
    }

    public AnalyticsConfiguration getAnalytics() {
        return analytics;
    }

    public void setAnalytics(AnalyticsConfiguration analytics) {
        this.analytics = analytics;
    }

    public DesktopConfiguration getDesktop() {
        return desktop;
    }

    public void setDesktop(DesktopConfiguration desktop) {
        this.desktop = desktop;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public ExperimentsConfiguration getExperiments() {
        return experiments;
    }

    public void setExperiments(ExperimentsConfiguration experiments) {
        this.experiments = experiments;
    }
}
