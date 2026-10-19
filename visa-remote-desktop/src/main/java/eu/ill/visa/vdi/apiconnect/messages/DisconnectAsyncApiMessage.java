package eu.ill.visa.vdi.apiconnect.messages;


import eu.ill.visa.common.vdi.apiconnect.exceptions.ApiCommunicationException;
import eu.ill.visa.common.vdi.domain.ConnectedUser;

import static java.lang.String.format;

public record DisconnectAsyncApiMessage(String connectionId, String clientId, ConnectedUser connectedUser) implements AsyncApiMessage {

    @Override
    public Type type() {
        return Type.DISCONNECT;
    }

    @Override
    public String formatError(ApiCommunicationException error) {
        return format("Failed to communicate disconnection of %s to API Server: %s", connectedUser.fullName(), error.getMessage());
    }
}
