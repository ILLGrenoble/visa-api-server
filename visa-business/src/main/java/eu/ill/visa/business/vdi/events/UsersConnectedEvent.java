package eu.ill.visa.business.vdi.events;

import eu.ill.visa.common.vdi.domain.ConnectedUser;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record UsersConnectedEvent(List<ConnectedUser> users, Long instanceId) {
}
