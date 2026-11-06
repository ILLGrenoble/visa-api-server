package eu.ill.visa.vdi.gateway.events;

import eu.ill.visa.common.vdi.domain.ConnectedUser;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AccessRequestEvent(String connectionId, ConnectedUser user, String requesterClientId) {
}
