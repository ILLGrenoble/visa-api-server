package eu.ill.visa.core.entity;


import jakarta.persistence.*;

import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "desktopSessionRttSample.getByInstanceSessionMemberId", query = """
        SELECT s FROM DesktopSessionRttSample s
        WHERE s.instanceSessionMemberId = :instanceSessionMemberId
        ORDER BY s.date
    """),
    @NamedQuery(name = "desktopSessionRttSample.getByInstanceSessionMemberIds", query = """
        SELECT s FROM DesktopSessionRttSample s
        WHERE s.instanceSessionMemberId IN :instanceSessionMemberIds
        ORDER BY s.date
    """),
    @NamedQuery(name = "desktopSessionRttSample.getByInstanceId", query = """
        SELECT s
        FROM DesktopSessionRttSample s
        LEFT JOIN InstanceSessionMember ism ON s.instanceSessionMemberId = ism.id
        LEFT JOIN Instance i ON ism.instanceSession.instanceId = i.id
        WHERE i.id IN :instanceId
        ORDER BY s.date
    """),
    @NamedQuery(name = "desktopSessionRttSample.getAllRequiringResampling", query = """
        SELECT s FROM DesktopSessionRttSample s
        WHERE s.date < :date
        AND s.samplePeriodMinutes < 60
        ORDER BY s.date
    """),
    @NamedQuery(name = "desktopSessionRttSample.deleteByIds", query = """
        DELETE FROM DesktopSessionRttSample s
        WHERE s.id IN :ids
    """),
    @NamedQuery(name = "desktopSessionRttSample.getByHypervisorSamplesSinceDate", query = """
        SELECT h, s
        FROM DesktopSessionRttSample s
        LEFT JOIN InstanceSessionMember ism ON s.instanceSessionMemberId = ism.id
        LEFT JOIN InstanceSession isess ON ism.instanceSession.id = isess.id
        LEFT JOIN Instance i ON isess.instanceId = i.id
        LEFT JOIN Hypervisor h ON i.hypervisorId = h.id
        WHERE s.date >= :date
        ORDER BY s.date
    """),
})
@Table(name = "desktop_session_rtt_sample")
public class DesktopSessionRttSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // Avoid costly request to get InstanceSessionMember: just use a standard column with the Id for these stats
    @Column(name = "instance_session_member_id", nullable = false)
    private Long instanceSessionMemberId;

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "sample_period_minutes", nullable = false)
    private Long samplePeriodMinutes;

    @Column(name = "client_mean_rtt_ms", nullable = true)
    private Double clientMeanRttMs;

    @Column(name = "client_sd_rtt_ms", nullable = true)
    private Double clientSdRttMs;

    @Column(name = "client_rtt_sample_count", nullable = false)
    private Long clientRttSampleCount;

    @Column(name = "instance_mean_rtt_ms", nullable = true)
    private Double instanceMeanRttMs;

    @Column(name = "instance_sd_rtt_ms", nullable = true)
    private Double instanceSdRttMs;

    @Column(name = "instance_rtt_sample_count", nullable = false)
    private Long instanceRttSampleCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstanceSessionMemberId() {
        return instanceSessionMemberId;
    }

    public void setInstanceSessionMemberId(Long instanceSessionMemberId) {
        this.instanceSessionMemberId = instanceSessionMemberId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getSamplePeriodMinutes() {
        return samplePeriodMinutes;
    }

    public void setSamplePeriodMinutes(Long samplePeriodMinutes) {
        this.samplePeriodMinutes = samplePeriodMinutes;
    }

    public Double getClientMeanRttMs() {
        return clientMeanRttMs;
    }

    public void setClientMeanRttMs(Double clientMeanRttMs) {
        this.clientMeanRttMs = clientMeanRttMs;
    }

    public Double getClientSdRttMs() {
        return clientSdRttMs;
    }

    public void setClientSdRttMs(Double clientSdRttMs) {
        this.clientSdRttMs = clientSdRttMs;
    }

    public Long getClientRttSampleCount() {
        return clientRttSampleCount;
    }

    public void setClientRttSampleCount(Long clientRttSampleCount) {
        this.clientRttSampleCount = clientRttSampleCount;
    }

    public Double getInstanceMeanRttMs() {
        return instanceMeanRttMs;
    }

    public void setInstanceMeanRttMs(Double instanceMeanRttMs) {
        this.instanceMeanRttMs = instanceMeanRttMs;
    }

    public Double getInstanceSdRttMs() {
        return instanceSdRttMs;
    }

    public void setInstanceSdRttMs(Double instanceSdRttMs) {
        this.instanceSdRttMs = instanceSdRttMs;
    }

    public Long getInstanceRttSampleCount() {
        return instanceRttSampleCount;
    }

    public void setInstanceRttSampleCount(Long instanceRttSampleCount) {
        this.instanceRttSampleCount = instanceRttSampleCount;
    }

    public static Builder Builder() {
        return new Builder();
    }

    public static class Builder {
        private Long instanceSessionMemberId;
        private Date date;
        private Long samplePeriodMinutes;
        private Double clientMeanRttMs;
        private Double clientSdRttMs;
        private Long clientRttSampleCount;
        private Double instanceMeanRttMs;
        private Double instanceSdRttMs;
        private Long instanceRttSampleCount;

        public Builder instanceSessionMemberId(Long instanceSessionMemberId) {
            this.instanceSessionMemberId = instanceSessionMemberId;
            return this;
        }

        public Builder date(Date date) {
            this.date = date;
            return this;
        }

        public Builder samplePeriodMinutes(Long samplePeriodMinutes) {
            this.samplePeriodMinutes = samplePeriodMinutes;
            return this;
        }

        public Builder clientMeanRttMs(Double clientMeanRttMs) {
            this.clientMeanRttMs = clientMeanRttMs;
            return this;
        }

        public Builder clientSdRttMs(Double clientSdRttMs) {
            this.clientSdRttMs = clientSdRttMs;
            return this;
        }

        public Builder clientRttSampleCount(Long clientRttSampleCount) {
            this.clientRttSampleCount = clientRttSampleCount;
            return this;
        }

        public Builder instanceMeanRttMs(Double instanceMeanRttMs) {
            this.instanceMeanRttMs = instanceMeanRttMs;
            return this;
        }

        public Builder instanceSdRttMs(Double instanceSdRttMs) {
            this.instanceSdRttMs = instanceSdRttMs;
            return this;
        }

        public Builder instanceRttSampleCount(Long instanceRttSampleCount) {
            this.instanceRttSampleCount = instanceRttSampleCount;
            return this;
        }

        public DesktopSessionRttSample build() {
            DesktopSessionRttSample sample = new DesktopSessionRttSample();
            sample.setInstanceSessionMemberId(instanceSessionMemberId);
            sample.setDate(date);
            sample.setSamplePeriodMinutes(samplePeriodMinutes);
            sample.setClientMeanRttMs(clientMeanRttMs);
            sample.setClientSdRttMs(clientSdRttMs);
            sample.setClientRttSampleCount(clientRttSampleCount);
            sample.setInstanceMeanRttMs(instanceMeanRttMs);
            sample.setInstanceSdRttMs(instanceSdRttMs);
            sample.setInstanceRttSampleCount(instanceRttSampleCount);
            return sample;
        }
    }
}
