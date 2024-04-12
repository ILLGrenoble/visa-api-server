package eu.ill.visa.core.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"id", "name", "version", "description", "url", "icon", "computeId", "createdAt", "deletedAt"})
public class Image extends Timestampable {

    private Long id;

    private String name;

    private String description;

    private String icon;

    private String computeId;

    private String version;

    private boolean deleted = false;

    private boolean visible = false;

    private String bootCommand;

    private String autologin;

    private List<ImageProtocol> protocols = new ArrayList<>();

    private CloudProviderConfiguration cloudProviderConfiguration;

    public Image() {
    }

    public Image(final Long id, final String name) {
        this(id, name, null);
    }

    public Image(final Long id, final String name, final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public CloudProviderConfiguration getCloudProviderConfiguration() {
        return cloudProviderConfiguration;
    }

    public void setCloudProviderConfiguration(CloudProviderConfiguration cloudProviderConfiguration) {
        this.cloudProviderConfiguration = cloudProviderConfiguration;
    }

    @Transient
    public Long getCloudId() {
        return this.cloudProviderConfiguration == null ? null : this.cloudProviderConfiguration.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Image image = (Image) o;

        return new EqualsBuilder()
            .append(id, image.id)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }

    public List<ImageProtocol> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<ImageProtocol> protocols) {
        this.protocols = protocols;
    }

    public void addProtocol(ImageProtocol protocol) {
        this.protocols.add(protocol);
    }

    public String getBootCommand() {
        return bootCommand;
    }

    public void setBootCommand(String bootCommand) {
        this.bootCommand = bootCommand;
    }

    public String getAutologin() {
        return autologin;
    }

    public void setAutologin(String autologin) {
        this.autologin = autologin;
    }

    public static final class Builder {
        private Long id;
        private String name;
        private String description;
        private String icon;
        private String computeId;
        private String version;
        private boolean visible = false;
        private String bootCommand;
        private String autologin;

        public Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder computeId(String computeId) {
            this.computeId = computeId;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Builder bootCommand(String bootCommand) {
            this.bootCommand = bootCommand;
            return this;
        }

        public Builder autologin(String autologin) {
            this.autologin = autologin;
            return this;
        }

        public Image build() {
            Image image = new Image();
            image.setId(id);
            image.setName(name);
            image.setDescription(description);
            image.setIcon(icon);
            image.setComputeId(computeId);
            image.setVersion(version);
            image.setVisible(visible);
            image.setAutologin(autologin);
            return image;
        }
    }
}
