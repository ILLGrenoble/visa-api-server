package eu.ill.visa.vdi.gateway.events;

public record AccessCancellationEvent(String userFullName, String requesterConnectionId) {
}
