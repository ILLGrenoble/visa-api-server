package eu.ill.visa.broker.domain.messages;

import eu.ill.visa.broker.domain.models.ClientEventCarrier;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ClientEventCarrierMessage(String clientId, ClientEventCarrier event) {
}
