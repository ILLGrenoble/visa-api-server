package eu.ill.visa.vdi.apiconnect.messages;


import eu.ill.visa.common.vdi.apiconnect.exceptions.ApiCommunicationException;

public interface AsyncApiMessage {
    public enum Type {
        DISCONNECT,
        CONNECTION_FAILED,
        UPDATE_CLIENT_ACTIVITY,
        CREATE_RTT_SAMPLES
    }

    Type type();
    String formatError(ApiCommunicationException error);
}
