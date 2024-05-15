package eu.ill.visa.web.graphqlxx.types;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

import java.util.List;

public class ImageType {

    @AdaptToScalar(Scalar.Int.class)
    private Long id;
    private String name;
    private String version;
    private String description;
    private String icon;
    private String computeId;
    private CloudImageType cloudImage;
    private Boolean visible;
    private List<ImageProtocolType> protocols;
    private String bootCommand;
    private String autologin;
    private CloudClientType cloudClient;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public CloudImageType getCloudImage() {
        return cloudImage;
    }

    public void setCloudImage(CloudImageType cloudImage) {
        this.cloudImage = cloudImage;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public List<ImageProtocolType> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<ImageProtocolType> protocols) {
        this.protocols = protocols;
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

    public CloudClientType getCloudClient() {
        return cloudClient;
    }

    public void setCloudClient(CloudClientType cloudClient) {
        this.cloudClient = cloudClient;
    }
}
