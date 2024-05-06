package eu.ill.visa.web.graphql.queries.inputs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    private Long cloudId;

    @NotNull
    private String computeId;

    private Boolean visible;

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

}
