package eu.ill.visa.broker.gateway;

public interface ClientEventSubscriber<T> {
    void onEvent(final String clientId, final T message);
}
