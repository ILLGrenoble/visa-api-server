package eu.ill.visa.vdi.brokers.messages;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record UserConnectedMessage(Long sessionId, String desktopSessionMemberId, ConnectedUser user) {
}
