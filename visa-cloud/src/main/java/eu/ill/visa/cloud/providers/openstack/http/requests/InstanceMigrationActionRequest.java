package eu.ill.visa.cloud.providers.openstack.http.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstanceMigrationActionRequest extends InstanceActionRequest {
    @JsonProperty("os-migrateLive")
    public MigrationData migrationData = null;

    public InstanceMigrationActionRequest(String host, boolean blockMigration, boolean diskOverCommit) {
        this.migrationData =  new MigrationData(host, blockMigration, diskOverCommit);
    }

    public record MigrationData(String host, @JsonProperty("block_migration") boolean blockMigration, @JsonProperty("disk_over_commit") boolean diskOverCommit) {
    }
}
