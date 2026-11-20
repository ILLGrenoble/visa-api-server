package eu.ill.visa.vdi.apiconnect.messages;


import eu.ill.visa.common.vdi.apiconnect.exceptions.ApiCommunicationException;
import eu.ill.visa.common.vdi.domain.VdiRttSample;

import java.util.List;

import static java.lang.String.format;

public record CreateRttSamplesAsyncApiMessage(List<VdiRttSample> samples) implements AsyncApiMessage {
    @Override
    public Type type() {
        return Type.CREATE_RTT_SAMPLES;
    }

    @Override
    public String formatError(ApiCommunicationException error) {
        return format("Failed to store %d samples: %s", samples.size(), error.getMessage());
    }
}
