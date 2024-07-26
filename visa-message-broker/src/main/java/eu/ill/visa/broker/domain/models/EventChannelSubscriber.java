package eu.ill.visa.broker.domain.models;

public record EventChannelSubscriber(String clientId, String userId, EventHandler eventHandler) {

    public void onEvent(Object event) {
        this.eventHandler.onEvent(event);
    }

    interface EventHandler {
        void onEvent(Object event);
    }
}
