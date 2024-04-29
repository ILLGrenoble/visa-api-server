package eu.ill.visa.core.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "cloud_provider_configuration")
public class CloudProviderConfiguration extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "type", length = 100, nullable = false)
    private String type;

    @Column(name = "name", length = 100, nullable = true)
    private String name;

    @Column(name = "server_name_prefix", length = 100, nullable = false)
    private String serverNamePrefix;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "cloud_provider_configuration_id", foreignKey = @ForeignKey(name = "fk_cloud_provider_configuration_id"), nullable = false)
    private List<CloudProviderConfigurationParameter> parameters = new ArrayList<>();

    @Column(name = "visible", nullable = false, columnDefinition = "")
    private boolean visible = false;

    @Column(name = "deleted_at", nullable = true)
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

    public void deleteParameters() {
        this.parameters = new ArrayList<>();
    }

    public void addParameter(CloudProviderConfigurationParameter parameter) {
        this.parameters.add(parameter);
    }

    public void setParameter(String key, String value) {
        CloudProviderConfigurationParameter parameter = this.parameters.stream().filter(cloudProviderConfigurationParameter -> cloudProviderConfigurationParameter.getKey().equals(key)).findFirst().orElse(null);
        if (parameter == null) {
            this.addParameter(new CloudProviderConfigurationParameter(key, value));
        } else {
            parameter.setValue(value);
        }
    }

    @Entity
    @Table(name = "cloud_provider_configuration_parameter")
    public static class CloudProviderConfigurationParameter extends Timestampable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @Column(name = "key", length = 256, nullable = false)
        private String key;

        @Column(name = "value", length = 8192, nullable = false)
        private String value;

        public CloudProviderConfigurationParameter() {
        }

        public CloudProviderConfigurationParameter(String key, String value) {
            this.key = key;
            this.value = value;
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


    public static final class Builder {
        private Long id;
        private String type;
        private String name;
        private String serverNamePrefix;
        private boolean visible = false;

        public Builder() {
        }

        public static CloudProviderConfiguration.Builder builder() {
            return new CloudProviderConfiguration.Builder();
        }

        public CloudProviderConfiguration.Builder id(Long id) {
            this.id = id;
            return this;
        }

        public CloudProviderConfiguration.Builder type(String type) {
            this.type = type;
            return this;
        }

        public CloudProviderConfiguration.Builder name(String name) {
            this.name = name;
            return this;
        }

        public CloudProviderConfiguration.Builder serverNamePrefix(String serverNamePrefix) {
            this.serverNamePrefix = serverNamePrefix;
            return this;
        }

        public CloudProviderConfiguration.Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public CloudProviderConfiguration build() {
            CloudProviderConfiguration cloudProviderConfiguration = new CloudProviderConfiguration();
            cloudProviderConfiguration.setId(id);
            cloudProviderConfiguration.setType(type);
            cloudProviderConfiguration.setName(name);
            cloudProviderConfiguration.setServerNamePrefix(serverNamePrefix);
            cloudProviderConfiguration.setVisible(visible);
            return cloudProviderConfiguration;
        }

    }
}
