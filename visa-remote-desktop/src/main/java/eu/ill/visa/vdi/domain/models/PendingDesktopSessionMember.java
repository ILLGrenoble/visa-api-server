package eu.ill.visa.vdi.domain.models;

public record PendingDesktopSessionMember(
    ConnectedUser connectedUser,
    SessionEventConnection sessionEventConnection,
    Long instanceId,
    String protocol,
    String token
) {
}
