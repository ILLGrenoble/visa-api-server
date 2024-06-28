package eu.ill.visa.vdi.gateway.dispatcher;

import eu.ill.visa.vdi.domain.models.SocketClient;

import java.util.ArrayList;
import java.util.List;

public class ClientEventSubscriptionList<T> {

    private final List<ClientEventSubscriber<T>> subscribers = new ArrayList<>();

    private final String type;
    private final Class<T> eventClass;

    public ClientEventSubscriptionList(final String type, final Class<T> eventClass) {
        this.type = type;
        this.eventClass = eventClass;
    }

    public String getType() {
        return this.type;
    }

    public Class<T> getEventClass() {
        return eventClass;
    }

    public ClientEventSubscriptionList<T> next(ClientEventSubscriber<T> subscriber) {
        this.subscribers.add(subscriber);
        return this;
    }

    public void onEvent(final SocketClient client, final T message) {
        this.subscribers.forEach(subscriber -> subscriber.onEvent(client, message));
    }
}

