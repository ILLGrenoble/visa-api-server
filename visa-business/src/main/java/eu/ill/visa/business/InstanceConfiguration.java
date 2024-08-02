package eu.ill.visa.business;

public interface InstanceConfiguration {

    Integer userMaxLifetimeDurationHours();
    Integer staffMaxLifetimeDurationHours();
    Integer userMaxInactivityDurationHours();
    Integer staffMaxInactivityDurationHours();
    Integer defaultUserInstanceQuota();
    Integer activityRetentionPeriodDays();
    Integer portCheckTimeoutMs();

}
