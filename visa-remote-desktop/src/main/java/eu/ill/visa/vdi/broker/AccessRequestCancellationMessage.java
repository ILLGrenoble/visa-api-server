package eu.ill.visa.vdi.broker;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record AccessRequestCancellationMessage(Long sessionId, ConnectedUser user, String requesterClientId) {
}
