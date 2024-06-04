package eu.ill.visa.cloud.providers.openstack.http.responses;

public record ServerCreationResponse(CreatedServer server) {
    public static record CreatedServer(String id) { }
}
