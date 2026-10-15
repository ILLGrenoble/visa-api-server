package eu.ill.visa.common.vdi.broker;

import eu.ill.visa.common.vdi.domain.ConnectedUser;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UserConnectedMessage(String connectionId, String clientId, ConnectedUser user) {
}
