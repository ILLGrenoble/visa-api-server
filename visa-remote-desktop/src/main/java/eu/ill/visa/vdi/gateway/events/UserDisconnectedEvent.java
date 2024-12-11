package eu.ill.visa.vdi.gateway.events;

import eu.ill.visa.vdi.domain.models.ConnectedUser;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UserDisconnectedEvent(ConnectedUser user, Long instanceId) {
}
