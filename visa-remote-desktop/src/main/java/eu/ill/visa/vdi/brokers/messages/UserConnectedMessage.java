package eu.ill.visa.vdi.brokers.messages;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record UserConnectedMessage(Long instanceId, ConnectedUser user, String connectionId) {
}
