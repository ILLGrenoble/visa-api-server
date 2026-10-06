package eu.ill.visa.web.rest.controllers.vdi.dto;


import java.util.Date;

public record VdiRttSample(Long instanceSessionId,
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
