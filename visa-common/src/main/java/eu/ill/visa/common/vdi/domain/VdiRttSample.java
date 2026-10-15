package eu.ill.visa.common.vdi.domain;


import java.util.Date;
public record VdiRttSample(String connectionId,
                           String clientId,
                           Date date,
                           Long samplePeriodMinutes,
                           Double clientMeanRttMs,
                           Double clientSdRttMs,
                           Long clientRttSampleCount,
                           Double instanceMeanRttMs,
                           Double instanceSdRttMs,
                           Long instanceRttSampleCount) {

    public static Builder Builder() {
        return new Builder();
    }

    public static class Builder {
        private String connectionId;
        private String clientId;
        private Date date;
        private Long samplePeriodMinutes;
        private Double clientMeanRttMs;
        private Double clientSdRttMs;
        private Long clientRttSampleCount;
        private Double instanceMeanRttMs;
        private Double instanceSdRttMs;
        private Long instanceRttSampleCount;

        public Builder connectionId(String connectionId) {
            this.connectionId = connectionId;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
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

        public VdiRttSample build() {
            return new VdiRttSample(
                    connectionId,
                    clientId,
                    date,
                    samplePeriodMinutes,
                    clientMeanRttMs,
                    clientSdRttMs,
                    clientRttSampleCount,
                    instanceMeanRttMs,
                    instanceSdRttMs,
                    instanceRttSampleCount);
        }
    }
}
