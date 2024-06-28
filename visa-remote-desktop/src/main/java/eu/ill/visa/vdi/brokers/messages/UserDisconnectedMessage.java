package eu.ill.visa.vdi.brokers.messages;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record UserDisconnectedMessage(Long sessionId, String desktopSessionMemberId, ConnectedUser user) {
}
