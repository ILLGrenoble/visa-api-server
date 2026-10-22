package eu.ill.visa.web.graphql.types;


import eu.ill.visa.core.entity.DesktopSessionRttSample;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("DesktopSessionRttSample")
public class DesktopSessionRttSampleType {
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long sessionId;
    private final @NotNull Date date;
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long samplePeriodMinutes;
    private final Double clientMeanRttMs;
    private final Double clientSdRttMs;
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long clientRttSampleCount;
    private final Double instanceMeanRttMs;
    private final Double instanceSdRttMs;
    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long instanceRttSampleCount;

    public DesktopSessionRttSampleType(DesktopSessionRttSample sample) {
        this.id = sample.getId();
        this.sessionId = sample.getSessionId();
        this.date = sample.getDate();
        this.samplePeriodMinutes = sample.getSamplePeriodMinutes();
        this.clientMeanRttMs = sample.getClientMeanRttMs();
        this.clientSdRttMs = sample.getClientSdRttMs();
        this.clientRttSampleCount = sample.getClientRttSampleCount();
        this.instanceMeanRttMs = sample.getInstanceMeanRttMs();
        this.instanceSdRttMs = sample.getInstanceSdRttMs();
        this.instanceRttSampleCount = sample.getInstanceRttSampleCount();
    }

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Date getDate() {
        return date;
    }

    public Long getSamplePeriodMinutes() {
        return samplePeriodMinutes;
    }

    public Double getClientMeanRttMs() {
        return clientMeanRttMs;
    }

    public Double getClientSdRttMs() {
        return clientSdRttMs;
    }

    public Long getClientRttSampleCount() {
        return clientRttSampleCount;
    }

    public Double getInstanceMeanRttMs() {
        return instanceMeanRttMs;
    }

    public Double getInstanceSdRttMs() {
        return instanceSdRttMs;
    }

    public Long getInstanceRttSampleCount() {
        return instanceRttSampleCount;
    }
}
