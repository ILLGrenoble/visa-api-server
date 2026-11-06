package eu.ill.visa.common.vdi.domain;



public record VdiInstance(
    Long id,
    Integer screenWidth,
    Integer screenHeight,
    String keyboardLayout,
    String username,
    String autoLogin,
    String ipAddress,
    Integer port,
    String secondaryVdiProtocol,
    boolean canConnectWhileOwnerAway,
    boolean ownerIsExternalUser
) {

}
