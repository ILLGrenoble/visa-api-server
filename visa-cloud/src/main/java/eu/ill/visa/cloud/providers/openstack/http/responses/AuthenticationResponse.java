package eu.ill.visa.cloud.providers.openstack.http.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;


@RegisterForReflection
@JsonRootName(value = "token")
public class AuthenticationResponse {

    @JsonProperty("expires_at")
    private Date expiresAt;

    @JsonProperty("issued_at")
    private Date issuedAt;

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }

    @JsonIgnore
    public long getHalfDurationMs() {
        long duration = this.expiresAt.getTime() - this.issuedAt.getTime();
        return duration / 2;
    }
}
