package eu.ill.visa.broker;

public interface MessageBroker {
    <T> MessageSubscriptionList<T> subscribe(Class<T> clazz);
    <T> void broadcast(T message);
    void shutdown();
}
