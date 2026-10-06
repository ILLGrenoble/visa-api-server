package eu.ill.visa.vdi.provider.models;


import eu.ill.visa.core.entity.ImageProtocol;
import eu.ill.visa.core.entity.Instance;

public class VdiInstance {
    private final Long id;
    private final Integer screenWidth;
    private final Integer screenHeight;
    private final String keyboardLayout;
    private final String username;
    private final String autoLogin;
    private final String ipAddress;
    private final String secondaryVdiProtocol;
    private final Integer port;
    private final boolean canConnectWhileOwnerAway;
    private final boolean ownerIsExternalUser;

    public VdiInstance(final Instance instance, final Integer port, boolean canConnectWhileOwnerAway, boolean ownerIsExternalUser) {
        this.id = instance.getId();
        this.screenWidth = instance.getScreenWidth();
        this.screenHeight = instance.getScreenHeight();
        this.keyboardLayout = instance.getKeyboardLayout();
        this.username = instance.getUsername();
        this.autoLogin = instance.getPlan().getImage().getAutologin();
        this.ipAddress = instance.getIpAddress();
        final ImageProtocol secondaryVdiProtocol = instance.getPlan().getImage().getSecondaryVdiProtocol();
        this.secondaryVdiProtocol = secondaryVdiProtocol != null ? secondaryVdiProtocol.getName() : null;

        this.port = port;
        this.canConnectWhileOwnerAway = canConnectWhileOwnerAway;
        this.ownerIsExternalUser = ownerIsExternalUser;
    }

    public Long getId() {
        return id;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public String getKeyboardLayout() {
        return keyboardLayout;
    }

    public String getUsername() {
        return username;
    }

    public String getAutoLogin() {
        return autoLogin;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getSecondaryVdiProtocol() {
        return secondaryVdiProtocol;
    }

    public Integer getPort() {
        return port;
    }

    public boolean isCanConnectWhileOwnerAway() {
        return canConnectWhileOwnerAway;
    }

    public boolean isOwnerIsExternalUser() {
        return ownerIsExternalUser;
    }

}
