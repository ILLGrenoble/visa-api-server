package eu.ill.visa.broker;

public interface MessageHandler<T> {
    void onMessage(T message);
}
