package eu.ill.visa.broker.domain.models;

public record EventChannelSubscription(String clientId, String userId, EventHandler eventHandler) {

    public void onEvent(Object event) {
        this.eventHandler.onEvent(event);
    }

}
