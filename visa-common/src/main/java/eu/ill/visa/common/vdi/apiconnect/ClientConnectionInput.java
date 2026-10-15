package eu.ill.visa.common.vdi.apiconnect;


import eu.ill.visa.common.vdi.domain.ConnectedUser;

public record ClientConnectionInput(Long instanceId, String protocol, String connectionId, String clientId, ConnectedUser user) {
}
