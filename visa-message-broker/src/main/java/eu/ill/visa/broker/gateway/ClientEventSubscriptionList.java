package eu.ill.visa.broker.gateway;

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

    public void onEvent(final String clientId, final T message) {
        this.subscribers.forEach(subscriber -> subscriber.onEvent(clientId, message));
    }
}

