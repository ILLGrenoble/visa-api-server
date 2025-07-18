package eu.ill.visa.web.graphql.inputs;

import eu.ill.visa.core.entity.Image;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.graphql.Input;

import java.util.List;

@Input("ImageInput")
public class ImageInput {

    @Size(min = 1, max = 250)
    private @NotNull String name;
    @Size(max = 100)
    private String version;
    @Size(max = 2500)
    private String description;
    private String icon;
    private @AdaptToScalar(Scalar.Int.class) Long cloudId;
    private @NotNull String computeId;
    private @NotNull Boolean visible;
    private @AdaptToScalar(Scalar.Int.class) List<Long> protocolIds;
    @Size(max=16000)
    private String bootCommand;
    private String autologin;
    private @NotNull @AdaptToScalar(Scalar.Int.class) Long defaultVdiProtocolId;
    private @AdaptToScalar(Scalar.Int.class) Long secondaryVdiProtocolId;
    private Image.AutoAcceptExtensionRequest autoAcceptExtensionRequest;

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

    public Long getCloudId() {
        return cloudId;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public List<Long> getProtocolIds() {
        return protocolIds;
    }

    public void setProtocolIds(List<Long> protocolIds) {
        this.protocolIds = protocolIds;
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

    public Long getDefaultVdiProtocolId() {
        return defaultVdiProtocolId;
    }

    public void setDefaultVdiProtocolId(Long defaultVdiProtocolId) {
        this.defaultVdiProtocolId = defaultVdiProtocolId;
    }

    public Long getSecondaryVdiProtocolId() {
        return secondaryVdiProtocolId;
    }

    public void setSecondaryVdiProtocolId(Long secondaryVdiProtocolId) {
        this.secondaryVdiProtocolId = secondaryVdiProtocolId;
    }

    public Image.AutoAcceptExtensionRequest getAutoAcceptExtensionRequest() {
        return autoAcceptExtensionRequest;
    }

    public void setAutoAcceptExtensionRequest(Image.AutoAcceptExtensionRequest autoAcceptExtensionRequest) {
        this.autoAcceptExtensionRequest = autoAcceptExtensionRequest;
    }
}
