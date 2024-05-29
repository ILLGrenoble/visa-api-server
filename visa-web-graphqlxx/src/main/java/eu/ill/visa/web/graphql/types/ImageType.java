package eu.ill.visa.web.graphql.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.visa.core.entity.Image;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

import java.util.List;

public class ImageType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String name;
    private final String version;
    private final String description;
    private final String icon;
    private final String computeId;
    private final Boolean visible;
    private final List<ImageProtocolType> protocols;
    private final String bootCommand;
    private final String autologin;
    private final Long cloudId;

    public ImageType(final Image image) {
        this.id = image.getId();
        this.name = image.getName();
        this.version = image.getVersion();
        this.description = image.getDescription();
        this.icon = image.getIcon();
        this.computeId = image.getComputeId();
        this.protocols = image.getProtocols().stream().map(ImageProtocolType::new).toList();
        this.visible = image.isVisible();
        this.bootCommand = image.getBootCommand();
        this.autologin = image.getAutologin();
        this.cloudId = image.getCloudId();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getComputeId() {
        return computeId;
    }

    public Boolean getVisible() {
        return visible;
    }

    public List<ImageProtocolType> getProtocols() {
        return protocols;
    }

    public String getBootCommand() {
        return bootCommand;
    }

    public String getAutologin() {
        return autologin;
    }

    @JsonIgnore
    public Long getCloudId() {
        return cloudId;
    }
}
