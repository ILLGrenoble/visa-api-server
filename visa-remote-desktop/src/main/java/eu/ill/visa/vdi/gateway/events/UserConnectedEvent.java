package eu.ill.visa.vdi.gateway.events;

import eu.ill.visa.common.vdi.domain.ConnectedUser;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UserConnectedEvent(ConnectedUser user, Long instanceId) {
}
