package eu.ill.visa.broker.domain.messages;

import eu.ill.visa.broker.domain.models.ClientEventCarrier;

public record ClientEventCarrierMessage(String clientId, ClientEventCarrier event) {
}
