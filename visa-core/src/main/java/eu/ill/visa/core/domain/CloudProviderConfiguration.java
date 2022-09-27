package eu.ill.visa.core.domain;

import java.util.Date;
import java.util.List;

public class CloudProviderConfiguration extends Timestampable {

    private Long id;
    private String type;
    private String name;
    private String serverNamePrefix;
    private List<CloudProviderConfigurationParameter> parameters;

    private boolean visible = false;
    private Date deletedAt;

    public CloudProviderConfiguration() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getServerNamePrefix() {
        return serverNamePrefix;
    }

    public void setServerNamePrefix(String serverNamePrefix) {
        this.serverNamePrefix = serverNamePrefix;
    }

    public List<CloudProviderConfigurationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<CloudProviderConfigurationParameter> parameters) {
        this.parameters = parameters;
    }

    public static class CloudProviderConfigurationParameter extends Timestampable {
        private Long id;
        private String key;
        private String value;

        public CloudProviderConfigurationParameter() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
