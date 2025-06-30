package eu.ill.visa.core.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"id", "name", "version", "description", "url", "icon", "computeId", "createdAt", "deletedAt"})
@Entity
@NamedQueries({
    @NamedQuery(name = "image.getById", query = """
            SELECT i FROM Image i
            WHERE i.id = :id
            AND i.deleted = false
    """),
    @NamedQuery(name = "image.getAll", query = """
            SELECT i
            FROM Image i
            LEFT JOIN i.cloudProviderConfiguration cpc
            WHERE i.deleted = false
            AND i.visible = true
            AND cpc.deletedAt IS NULL
            AND COALESCE(cpc.visible, true) = true
            ORDER BY i.id
    """),
    @NamedQuery(name = "image.getAllForAdmin", query = """
            SELECT i
            FROM Image i
            LEFT JOIN FETCH i.cloudProviderConfiguration cpc
            LEFT JOIN FETCH i.protocols p
            WHERE i.deleted = false
            AND cpc.deletedAt IS NULL
            ORDER BY i.id
    """),
    @NamedQuery(name = "image.countAllForAdmin", query = """
            SELECT count(distinct im.id)
            FROM Image im, Plan p, Instance i
            where p.image = im
            and i.plan = p
            and i.deletedAt IS NULL
    """),
})
@Table(name = "image")
public class Image extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @Column(name = "description", length = 2500, nullable = true)
    private String description;

    @Column(name = "icon", length = 100, nullable = false)
    private String icon;

    @Column(name = "compute_id", length = 250, nullable = false)
    private String computeId;

    @Column(name = "version", length = 100, nullable = true)
    private String version;

    @Column(name = "deleted", nullable = false, columnDefinition = "")
    private boolean deleted = false;

    @Column(name = "visible", nullable = false, columnDefinition = "")
    private boolean visible = false;

    @Column(name = "boot_command", nullable = true, columnDefinition = "TEXT")
    private String bootCommand;

    @Column(name = "autologin", nullable = true)
    private String autologin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "image_protocol",
        joinColumns = @JoinColumn(name = "image_id", foreignKey = @ForeignKey(name = "fk_image_id")),
        inverseJoinColumns = @JoinColumn(name = "protocol_id", foreignKey = @ForeignKey(name = "fk_protocol_id"))
    )
    private List<ImageProtocol> protocols = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "default_vdi_protocol", foreignKey = @ForeignKey(name = "fk_default_vdi_protocol_id"), nullable = true)
    private ImageProtocol defaultVdiProtocol;

    @ManyToOne
    @JoinColumn(name = "secondary_vdi_protocol", foreignKey = @ForeignKey(name = "fk_secondary_vdi_protocol_id"), nullable = true)
    private ImageProtocol secondaryVdiProtocol;

    @ManyToOne
    @JoinColumn(name = "cloud_provider_configuration_id", foreignKey = @ForeignKey(name = "fk_cloud_provider_configuration_id"), nullable = true)
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

    public ImageProtocol getDefaultVdiProtocol() {
        return this.defaultVdiProtocol;
    }

    public void setDefaultVdiProtocol(ImageProtocol defaultVdiProtocol) {
        this.defaultVdiProtocol = defaultVdiProtocol;
    }

    public ImageProtocol getSecondaryVdiProtocol() {
        return secondaryVdiProtocol;
    }

    public void setSecondaryVdiProtocol(ImageProtocol secondaryVdiProtocol) {
        this.secondaryVdiProtocol = secondaryVdiProtocol;
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

    public ImageProtocol getProtocolByName(String protocolName) {
        return this.protocols.stream().filter(protocol -> protocol.getName().equals(protocolName)).findFirst().orElse(null);
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
