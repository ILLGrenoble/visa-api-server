package eu.ill.visa.vdi.brokers.messages;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record AccessRequestCancellationMessage(Long sessionId, ConnectedUser user, String requesterConnectionId) {
}
