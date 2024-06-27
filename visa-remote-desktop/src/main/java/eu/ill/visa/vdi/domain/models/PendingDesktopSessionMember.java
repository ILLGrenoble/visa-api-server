package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.core.entity.Instance;

public record PendingDesktopSessionMember(
    ConnectedUser connectedUser,
    SessionEventConnection sessionEventConnection,
    Instance instance,
    String protocol,
    String token
) {
}
