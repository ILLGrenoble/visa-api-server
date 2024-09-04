package eu.ill.visa.broker.domain.models;

import java.util.List;

public record EventChannelSubscription(String clientId, String userId, List<String> roles, EventHandler eventHandler) {

    public void onEvent(Object event) {
        this.eventHandler.onEvent(event);
    }

}
