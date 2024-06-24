package eu.ill.visa.vdi.brokers;

import java.util.ArrayList;
import java.util.List;

public class RemoteDesktopMessageSubscriptionList<T> {

    private final List<RemoteDesktopMessageHandler<T>> subscribers = new ArrayList<>();

    private final Class<T> clazz;

    public RemoteDesktopMessageSubscriptionList(final Class<T> clazz) {
        this.clazz = clazz;
    }

    public String getClazzName() {
        return this.clazz.getName();
    }

    public RemoteDesktopMessageSubscriptionList<T> next(RemoteDesktopMessageHandler<T> subscriber) {
        this.subscribers.add(subscriber);
        return this;
    }

    public void onMessage(T message) {
        this.subscribers.forEach(subscriber -> subscriber.onMessage(message));
    }
}

