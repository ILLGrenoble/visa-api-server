package eu.ill.visa.core.domain;

import eu.ill.visa.core.domain.enumerations.HealthStatus;

public class HealthReport {

    private HealthStatus globalStatus;
    private HealthStatus cloudStatus;
    private HealthStatus databaseStatus;

    public HealthReport(HealthStatus globalStatus, HealthStatus cloudStatus, HealthStatus databaseStatus) {
        this.globalStatus = globalStatus;
        this.cloudStatus = cloudStatus;
        this.databaseStatus = databaseStatus;
    }

    public HealthStatus getGlobalStatus() {
        return globalStatus;
    }

    public void setGlobalStatus(HealthStatus globalStatus) {
        this.globalStatus = globalStatus;
    }

    public HealthStatus getCloudStatus() {
        return cloudStatus;
    }

    public void setCloudStatus(HealthStatus cloudStatus) {
        this.cloudStatus = cloudStatus;
    }

    public HealthStatus getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(HealthStatus databaseStatus) {
        this.databaseStatus = databaseStatus;
    }
}
