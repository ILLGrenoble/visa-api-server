package eu.ill.visa.web.rest.controllers.vdi.dto;


import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record ClientConnectionInput(Long instanceId, String protocol, String connectionId, String clientId, ConnectedUser user) {
}
