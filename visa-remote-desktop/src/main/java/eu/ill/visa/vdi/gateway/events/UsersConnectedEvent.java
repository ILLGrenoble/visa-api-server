package eu.ill.visa.vdi.gateway.events;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

import java.util.List;

public record UsersConnectedEvent(List<ConnectedUser> users, Long instanceId) {
}
