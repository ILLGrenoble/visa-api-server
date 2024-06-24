package eu.ill.visa.vdi.gateway.events;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record AccessRequest(Long instanceId, ConnectedUser user, String requesterConnectionId) {
}
