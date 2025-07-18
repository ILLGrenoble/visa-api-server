package eu.ill.visa.web.rest.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InstanceUpdatorDto {

    @NotNull
    @Size(min = 1, max = 250)
    private String name;

    @Size(max = 2500)
    private String comments;

    @NotNull
    private Integer screenWidth;

    @NotNull
    private Integer screenHeight;

    @NotNull
    @Size(max = 250)
    private String keyboardLayout;

    @NotNull
    private Boolean unrestrictedAccess;

    @NotNull
    private Long vdiProtocolId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }

    public String getKeyboardLayout() {
        return keyboardLayout;
    }

    public void setKeyboardLayout(String keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }

    public Boolean getUnrestrictedAccess() {
        return unrestrictedAccess;
    }

    public void setUnrestrictedAccess(Boolean unrestrictedAccess) {
        this.unrestrictedAccess = unrestrictedAccess;
    }

    public Long getVdiProtocolId() {
        return vdiProtocolId;
    }

    public void setVdiProtocolId(Long vdiProtocolId) {
        this.vdiProtocolId = vdiProtocolId;
    }
}
