package eu.ill.visa.vdi.gateway.dispatcher;

import eu.ill.visa.vdi.domain.models.SocketClient;

public interface ClientEventSubscriber<T> {
    void onEvent(final SocketClient client, final T message);
}
