package eu.ill.visa.vdi.broker;

import eu.ill.visa.vdi.domain.models.ConnectedUser;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AccessRequestCancellationMessage(String connectionId, ConnectedUser user, String requesterClientId) {
}
