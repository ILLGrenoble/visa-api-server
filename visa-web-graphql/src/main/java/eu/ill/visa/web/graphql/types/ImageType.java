package eu.ill.visa.web.graphql.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.visa.core.entity.Image;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

@Type("Image")
public class ImageType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String name;
    private final String version;
    private final String description;
    private final String icon;
    private final @NotNull String computeId;
    private final @NotNull Boolean visible;
    private final List<ImageProtocolType> protocols;
    private final ImageProtocolType defaultVdiProtocol;
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
        this.defaultVdiProtocol = image.getDefaultVdiProtocol() != null ? new ImageProtocolType(image.getDefaultVdiProtocol()) : null;
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

    public ImageProtocolType getDefaultVdiProtocol() {
        return defaultVdiProtocol;
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
