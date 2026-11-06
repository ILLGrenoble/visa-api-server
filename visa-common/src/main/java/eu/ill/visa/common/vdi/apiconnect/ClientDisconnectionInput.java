package eu.ill.visa.common.vdi.apiconnect;


import eu.ill.visa.common.vdi.domain.ConnectedUser;

public record ClientDisconnectionInput(String connectionId, String clientId, ConnectedUser user) {
}
