package eu.ill.visa.vdi.apiconnect.messages;


import eu.ill.visa.common.vdi.apiconnect.exceptions.ApiCommunicationException;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.enums.InstanceActivityType;

import java.util.Date;

import static eu.ill.visa.common.utils.exceptions.ExceptionFormatter.formatException;
import static java.lang.String.format;

public record UpdateClientActivityAsyncApiMessage(Long instanceId, String connectionId, String clientId, ConnectedUser connectedUser, Date lastSeenAt, Date lastInteractionAt, InstanceActivityType instanceActivityType) implements AsyncApiMessage {

    @Override
    public Type type() {
        return Type.UPDATE_CLIENT_ACTIVITY;
    }

    @Override
    public String formatError(ApiCommunicationException error) {
        return format("Failed to update instance %d activity: %s", instanceId, formatException(error));
    }
}
