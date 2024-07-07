package eu.ill.visa.vdi.gateway.dispatcher;

import eu.ill.visa.vdi.domain.models.SocketClient;

public interface ClientConnectSubscriber {
    void onConnect(final SocketClient client);
}
