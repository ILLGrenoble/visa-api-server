package eu.ill.visa.web.graphql.queries.inputs;

import jakarta.validation.constraints.NotNull;

public class CloudClientInput {

    @NotNull
    private String type;

    @NotNull
    private String name;

    @NotNull
    private String serverNamePrefix;

    @NotNull
    private Boolean visible;

    private WebProviderConfigurationInput webProviderConfiguration;

    private OpenStackProviderConfigurationInput openStackProviderConfiguration;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServerNamePrefix() {
        return serverNamePrefix;
    }

    public void setServerNamePrefix(String serverNamePrefix) {
        this.serverNamePrefix = serverNamePrefix;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public WebProviderConfigurationInput getWebProviderConfiguration() {
        return webProviderConfiguration;
    }

    public void setWebProviderConfiguration(WebProviderConfigurationInput webProviderConfiguration) {
        this.webProviderConfiguration = webProviderConfiguration;
    }

    public OpenStackProviderConfigurationInput getOpenStackProviderConfiguration() {
        return openStackProviderConfiguration;
    }

    public void setOpenStackProviderConfiguration(OpenStackProviderConfigurationInput openStackProviderConfiguration) {
        this.openStackProviderConfiguration = openStackProviderConfiguration;
    }
}
