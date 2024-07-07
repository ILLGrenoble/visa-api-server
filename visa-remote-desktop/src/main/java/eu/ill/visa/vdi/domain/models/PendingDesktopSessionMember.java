package eu.ill.visa.vdi.domain.models;

public record PendingDesktopSessionMember(
    String token,
    ConnectedUser connectedUser,
    EventChannel eventChannel,
    Long instanceId,
    String protocol
) {
}
