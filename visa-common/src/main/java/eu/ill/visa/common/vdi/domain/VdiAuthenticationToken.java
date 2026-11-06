package eu.ill.visa.common.vdi.domain;


public record VdiAuthenticationToken(VdiInstance instance, ConnectedUser connectedUser, Boolean isPublicAccessToken, String activeConnectionId, Boolean isOwnerConnected) {
}
