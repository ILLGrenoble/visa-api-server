package eu.ill.visa.vdi.apiconnect.messages;


import eu.ill.visa.common.vdi.apiconnect.exceptions.ApiCommunicationException;
import eu.ill.visa.common.vdi.domain.ConnectedUser;

import static java.lang.String.format;

public record ConnectionFailedAsyncApiMessage(Long instanceId, String protocol, String connectionId, String clientId, ConnectedUser connectedUser) implements AsyncApiMessage {
    @Override
    public Type type() {
        return Type.CONNECTION_FAILED;
    }

    @Override
    public String formatError(ApiCommunicationException error) {
        return format("Failed to communicate connection error for instance %d to API Server: %s", instanceId, error.getMessage());
    }
}
