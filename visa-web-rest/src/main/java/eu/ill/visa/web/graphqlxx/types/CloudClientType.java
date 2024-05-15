package eu.ill.visa.web.graphqlxx.types;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class CloudClientType {

    @AdaptToScalar(Scalar.Int.class)
    private Long id;
    private String name;
    private String type;
    private String serverNamePrefix;
    private Boolean visible;
    private OpenStackProviderConfigurationType openStackProviderConfiguration;
    private WebProviderConfigurationType webProviderConfigurationType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public OpenStackProviderConfigurationType getOpenStackProviderConfiguration() {
        return openStackProviderConfiguration;
    }

    public void setOpenStackProviderConfiguration(OpenStackProviderConfigurationType openStackProviderConfiguration) {
        this.openStackProviderConfiguration = openStackProviderConfiguration;
    }

    public WebProviderConfigurationType getWebProviderConfigurationType() {
        return webProviderConfigurationType;
    }

    public void setWebProviderConfigurationType(WebProviderConfigurationType webProviderConfigurationType) {
        this.webProviderConfigurationType = webProviderConfigurationType;
    }
}
