package eu.ill.visa.web.graphql.types;


import eu.ill.visa.core.entity.DesktopSessionRttSample;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("DesktopSessionRttSample")
public class DesktopSessionRttSampleType {
    private final @NotNull Long id;
    private final @NotNull Long instanceSessionMemberId;
    private final @NotNull Date date;
    private final @NotNull Long samplePeriodMinutes;
    private final Double clientMeanRttMs;
    private final Double clientSdRttMs;
    private final @NotNull Long clientRttSampleCount;
    private final Double instanceMeanRttMs;
    private final Double instanceSdRttMs;
    private final @NotNull Long instanceRttSampleCount;

    public DesktopSessionRttSampleType(DesktopSessionRttSample sample) {
        this.id = sample.getId();
        this.instanceSessionMemberId = sample.getInstanceSessionMemberId();
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

    public Long getInstanceSessionMemberId() {
        return instanceSessionMemberId;
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
