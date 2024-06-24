package eu.ill.visa.vdi.gateway.events;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record UserConnectedEvent(ConnectedUser user, Long instanceId) {
}
