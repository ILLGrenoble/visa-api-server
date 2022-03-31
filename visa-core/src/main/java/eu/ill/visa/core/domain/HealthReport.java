package eu.ill.visa.core.domain;

public class HealthReport {

    private HealthState globalState;
    private HealthState cloudState;
    private HealthState databaseState;

    public HealthReport(HealthState globalState, HealthState cloudState, HealthState databaseState) {
        this.globalState = globalState;
        this.cloudState = cloudState;
        this.databaseState = databaseState;
    }

    public HealthState getGlobalState() {
        return globalState;
    }

    public void setGlobalState(HealthState globalState) {
        this.globalState = globalState;
    }

    public HealthState getCloudState() {
        return cloudState;
    }

    public void setCloudState(HealthState cloudState) {
        this.cloudState = cloudState;
    }

    public HealthState getDatabaseState() {
        return databaseState;
    }

    public void setDatabaseState(HealthState databaseState) {
        this.databaseState = databaseState;
    }
}
