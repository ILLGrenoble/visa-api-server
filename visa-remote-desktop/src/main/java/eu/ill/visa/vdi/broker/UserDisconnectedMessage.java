package eu.ill.visa.vdi.broker;

import eu.ill.visa.vdi.domain.models.ConnectedUser;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UserDisconnectedMessage(String connectionId, String clientId, ConnectedUser user) {
}
