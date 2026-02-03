package eu.ill.visa.cloud.providers.web.http.requests;

public record InstanceMigrationRequest(String host, Boolean blockMigration, Boolean diskOverCommit) {
}
