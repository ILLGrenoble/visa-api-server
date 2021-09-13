package eu.ill.visa.web.bundles.graphql.queries.inputs;

import eu.ill.visa.web.bundles.graphql.validation.Cloud;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class ImageInput {

    @NotNull
    @Size(min = 1, max = 250)
    private String name;

    @Size(max = 100)
    private String version;

    @Size(max = 2500)
    private String description;

    private String icon;

    @Cloud(type = "image")
    private String computeId;

    private Boolean visible;

    private Boolean deleted;

    private List<Long> protocolIds;

    @Size(max=16000)
    private String bootCommand;

    private String autologin;

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

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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

}
