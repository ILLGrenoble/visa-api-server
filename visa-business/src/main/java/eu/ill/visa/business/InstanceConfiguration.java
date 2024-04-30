package eu.ill.visa.business;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "business.instance", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface InstanceConfiguration {

    Integer userMaxLifetimeDurationHours();
    Integer staffMaxLifetimeDurationHours();
    Integer userMaxInactivityDurationHours();
    Integer staffMaxInactivityDurationHours();
    Integer defaultUserInstanceQuota();
    Integer activityRetentionPeriodDays();

}
