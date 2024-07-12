package eu.ill.visa.vdi.gateway.dispatcher;

import eu.ill.visa.vdi.domain.models.SocketClient;

public interface SocketConnectSubscriber {
    void onConnect(final SocketClient client);
}
