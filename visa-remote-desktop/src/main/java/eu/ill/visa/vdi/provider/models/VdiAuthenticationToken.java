package eu.ill.visa.vdi.provider.models;


import eu.ill.visa.vdi.domain.models.ConnectedUser;

public record VdiAuthenticationToken(VdiInstance instance, ConnectedUser connectedUser, boolean isPublicAccessToken, String activeConnectionId, Boolean isOwnerConnected) {

}
