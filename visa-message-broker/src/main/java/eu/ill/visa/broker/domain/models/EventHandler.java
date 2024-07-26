package eu.ill.visa.broker.domain.models;

public interface EventHandler {
    void onEvent(Object event);
}
