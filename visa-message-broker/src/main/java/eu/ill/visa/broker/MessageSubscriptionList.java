package eu.ill.visa.broker;

import java.util.ArrayList;
import java.util.List;

public class MessageSubscriptionList<T> {

    private final List<MessageHandler<T>> subscribers = new ArrayList<>();

    private final Class<T> clazz;

    public MessageSubscriptionList(final Class<T> clazz) {
        this.clazz = clazz;
    }

    public String getClazzName() {
        return this.clazz.getName();
    }

    public MessageSubscriptionList<T> next(MessageHandler<T> subscriber) {
        this.subscribers.add(subscriber);
        return this;
    }

    public void onMessage(T message) {
        this.subscribers.forEach(subscriber -> subscriber.onMessage(message));
    }
}

