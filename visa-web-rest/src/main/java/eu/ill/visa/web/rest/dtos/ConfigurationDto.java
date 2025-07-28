package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.business.InstanceConfiguration;
import eu.ill.visa.web.rest.*;

import java.util.List;
import java.util.Map;

public class ConfigurationDto {

    private final String version;
    private final String contactEmail;
    private final LoginConfigurationDto login;
    private final AnalyticsConfigurationDto analytics;
    private final DesktopConfigurationDto desktop;
    private final ExperimentsConfigurationDto experiments;
    private final InstanceConfigurationDto instance;
    private final Map<String, String> metadata;

    public ConfigurationDto(final ClientConfiguration clientConfiguration,
                            final DesktopConfigurationImpl desktopConfiguration,
                            final InstanceConfiguration instanceConfiguration,
                            final String version,
                            final Map<String, String> metadata) {
        this.contactEmail = clientConfiguration.contactEmail().orElse(null);
        this.login = new LoginConfigurationDto(clientConfiguration.loginConfiguration());
        this.analytics = new AnalyticsConfigurationDto(clientConfiguration.analyticsConfiguration());
        this.desktop = new DesktopConfigurationDto(desktopConfiguration);
        this.experiments = new ExperimentsConfigurationDto(clientConfiguration.experimentsConfiguration());
        this.instance = new InstanceConfigurationDto(instanceConfiguration);
        this.version = version;
        this.metadata = metadata;
    }

    public String getVersion() {
        return version;
    }

    public LoginConfigurationDto getLogin() {
        return login;
    }

    public AnalyticsConfigurationDto getAnalytics() {
        return analytics;
    }

    public DesktopConfigurationDto getDesktop() {
        return desktop;
    }

    public ExperimentsConfigurationDto getExperiments() {
        return experiments;
    }

    public InstanceConfigurationDto getInstance() {
        return instance;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public static class LoginConfigurationDto {
        private String issuer;
        private String clientId;
        private String scope;
        private boolean showDebugInformation;
        private boolean sessionChecksEnabled;

        public LoginConfigurationDto(final LoginConfiguration configuration) {
            this.issuer = configuration.issuer();
            this.clientId = configuration.clientId();
            this.scope = configuration.scope();
            this.showDebugInformation = configuration.showDebugInformation();
            this.sessionChecksEnabled = configuration.sessionChecksEnabled();
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public boolean isShowDebugInformation() {
            return showDebugInformation;
        }

        public void setShowDebugInformation(boolean showDebugInformation) {
            this.showDebugInformation = showDebugInformation;
        }

        public boolean isSessionChecksEnabled() {
            return sessionChecksEnabled;
        }

        public void setSessionChecksEnabled(boolean sessionChecksEnabled) {
            this.sessionChecksEnabled = sessionChecksEnabled;
        }
    }

    public static class AnalyticsConfigurationDto {
        private final Boolean enabled;
        private final String url;
        private final Integer siteId;

        public AnalyticsConfigurationDto(AnalyticsConfiguration configuration) {
            this.enabled = configuration.enabled();
            this.url = configuration.url().orElse(null);
            this.siteId = configuration.siteId().orElse(null);
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public String getUrl() {
            return url;
        }

        public Integer getSiteId() {
            return siteId;
        }
    }

    public static class DesktopConfigurationDto {
        private final List<DesktopConfigurationImpl.Host> allowedClipboardUrlHosts;
        private final List<DesktopConfigurationImpl.KeyboardLayout> keyboardLayouts;

        public DesktopConfigurationDto(final DesktopConfigurationImpl configuration) {
            this.allowedClipboardUrlHosts = configuration.getAllowedClipboardUrlHosts();
            this.keyboardLayouts = configuration.getKeyboardLayouts();
        }

        public List<DesktopConfigurationImpl.Host> getAllowedClipboardUrlHosts() {
            return allowedClipboardUrlHosts;
        }

        public List<DesktopConfigurationImpl.KeyboardLayout> getKeyboardLayouts() {
            return keyboardLayouts;
        }
    }

    public static class ExperimentsConfigurationDto {
        private final boolean openDataIncluded;

        public ExperimentsConfigurationDto(final ExperimentsConfiguration configuration) {
            this.openDataIncluded = configuration.openDataIncluded();
        }

        public boolean isOpenDataIncluded() {
            return openDataIncluded;
        }
    }

    public static class InstanceConfigurationDto {
        private final boolean publicAccessTokenEnabled;

        public InstanceConfigurationDto(final InstanceConfiguration configuration) {
            this.publicAccessTokenEnabled = configuration.publicAccessTokenEnabled();
        }

        public boolean isPublicAccessTokenEnabled() {
            return publicAccessTokenEnabled;
        }
    }
}
