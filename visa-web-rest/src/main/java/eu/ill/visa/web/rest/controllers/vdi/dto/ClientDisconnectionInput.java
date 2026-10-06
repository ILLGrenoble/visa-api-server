package eu.ill.visa.web.rest.controllers.vdi.dto;


import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record ClientDisconnectionInput(String connectionId, String clientId, ConnectedUser user) {
}
