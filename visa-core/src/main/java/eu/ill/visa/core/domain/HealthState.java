package eu.ill.visa.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import eu.ill.visa.core.domain.enumerations.HealthStatus;

public class HealthState {

    private HealthStatus status;
    private String message;

    public HealthState(HealthStatus status) {
        this.status = status;
    }

    public HealthState(HealthStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HealthStatus getStatus() {
        return status;
    }

    public void setStatus(HealthStatus status) {
        this.status = status;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public boolean isOk() {
        return this.status.equals(HealthStatus.OK);
    }
}
