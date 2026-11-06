package eu.ill.visa.common.vdi.broker;

import eu.ill.visa.common.vdi.domain.ConnectedUser;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AccessRequestCancellationMessage(String connectionId, ConnectedUser user, String requesterClientId) {
}
