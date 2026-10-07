package eu.ill.visa.vdi.provider.models;


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

}
