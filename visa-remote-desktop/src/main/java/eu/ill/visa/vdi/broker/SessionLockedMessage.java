package eu.ill.visa.vdi.broker;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record SessionLockedMessage(Long sessionId, ConnectedUser user) {
}
