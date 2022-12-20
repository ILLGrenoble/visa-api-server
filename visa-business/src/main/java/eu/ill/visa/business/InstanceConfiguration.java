package eu.ill.visa.business;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class InstanceConfiguration {

    private Integer userMaxLifetimeDurationHours = 336;
    private Integer staffMaxLifetimeDurationHours = 1440;
    private Integer userMaxInactivityDurationHours = 96;
    private Integer staffMaxInactivityDurationHours = 192;
    private Integer defaultUserInstanceQuota = 2;

    @JsonProperty
    @NotNull
    @Valid
    public Integer getUserMaxLifetimeDurationHours() {
        return userMaxLifetimeDurationHours;
    }

    public void setUserMaxLifetimeDurationHours(Integer userMaxLifetimeDurationHours) {
        this.userMaxLifetimeDurationHours = userMaxLifetimeDurationHours;
    }

    @JsonProperty
    @NotNull
    @Valid
    public Integer getStaffMaxLifetimeDurationHours() {
        return staffMaxLifetimeDurationHours;
    }

    public void setStaffMaxLifetimeDurationHours(Integer staffMaxLifetimeDurationHours) {
        this.staffMaxLifetimeDurationHours = staffMaxLifetimeDurationHours;
    }

    @JsonProperty
    @NotNull
    @Valid
    public Integer getUserMaxInactivityDurationHours() {
        return userMaxInactivityDurationHours;
    }

    public void setUserMaxInactivityDurationHours(Integer userMaxInactivityDurationHours) {
        this.userMaxInactivityDurationHours = userMaxInactivityDurationHours;
    }

    @JsonProperty
    @NotNull
    @Valid
    public Integer getStaffMaxInactivityDurationHours() {
        return staffMaxInactivityDurationHours;
    }

    public void setStaffMaxInactivityDurationHours(Integer staffMaxInactivityDurationHours) {
        this.staffMaxInactivityDurationHours = staffMaxInactivityDurationHours;
    }

    @JsonProperty
    @NotNull
    @Valid
    public Integer getDefaultUserInstanceQuota() {
        return defaultUserInstanceQuota;
    }

    public void setDefaultUserInstanceQuota(Integer defaultUserInstanceQuota) {
        this.defaultUserInstanceQuota = defaultUserInstanceQuota;
    }
}
