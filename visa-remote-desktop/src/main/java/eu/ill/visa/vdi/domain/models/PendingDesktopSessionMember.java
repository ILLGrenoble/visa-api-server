package eu.ill.visa.vdi.domain.models;

public record PendingDesktopSessionMember(
    String token,
    ConnectedUser connectedUser,
    SessionEventConnection sessionEventConnection,
    Long instanceId,
    String protocol
) {
}
