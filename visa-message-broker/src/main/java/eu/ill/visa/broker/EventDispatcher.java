package eu.ill.visa.broker;

import eu.ill.visa.broker.domain.models.BroadcastEventMessage;
import eu.ill.visa.broker.domain.models.ClientEventMessage;
import eu.ill.visa.broker.domain.models.EventChannelSubscriber;
import eu.ill.visa.broker.domain.models.UserEventMessage;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@Startup
@ApplicationScoped
public class EventDispatcher {

    private final MessageBroker messageBroker;

    private final List<EventChannelSubscriber> subscribers = new ArrayList<>();

    @Inject
    public EventDispatcher(final jakarta.enterprise.inject.Instance<MessageBroker> messageBrokerInstance) {
        this.messageBroker = messageBrokerInstance.get();

        this.messageBroker.subscribe(UserEventMessage.class).next(this::onUserEvent);
        this.messageBroker.subscribe(ClientEventMessage.class).next(this::onClientEvent);
        this.messageBroker.subscribe(BroadcastEventMessage.class).next(this::onBroadcastEvent);
    }

    public void subscribe(final EventChannelSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(final EventChannelSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public void sendUserEvent(final String userId, Object event) {
        this.messageBroker.broadcast(new UserEventMessage(userId, event));
    }

    public void sendClientEvent(final String clientId, Object event) {
        this.messageBroker.broadcast(new ClientEventMessage(clientId, event));
    }

    public void broadcastEvent(Object event) {
        this.messageBroker.broadcast(new BroadcastEventMessage(event));
    }

    private void onUserEvent(final UserEventMessage message) {
        this.subscribers.stream()
            .filter(subscriber -> subscriber.userId().equals(message.userId()))
            .forEach(subscriber -> subscriber.onEvent(message.event()));
    }

    private void onClientEvent(final ClientEventMessage message) {
        this.subscribers.stream()
            .filter(subscriber -> subscriber.clientId().equals(message.clientId()))
            .forEach(subscriber -> subscriber.onEvent(message.event()));
    }

    private void onBroadcastEvent(final BroadcastEventMessage message) {
        this.subscribers.forEach(subscriber -> subscriber.onEvent(message.event()));
    }

}
