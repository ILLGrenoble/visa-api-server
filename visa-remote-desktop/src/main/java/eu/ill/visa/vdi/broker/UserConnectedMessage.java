package eu.ill.visa.vdi.broker;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record UserConnectedMessage(Long sessionId, String desktopSessionMemberId, ConnectedUser user) {
}
