package eu.ill.visa.common.vdi.broker;

import eu.ill.visa.common.vdi.domain.ConnectedUser;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AccessRequestMessage(String connectionId, ConnectedUser user, String requesterClientId) {
}
