package eu.ill.visa.vdi.broker;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record AccessRequestMessage(Long sessionId, ConnectedUser user, String requesterClientId) {
}
