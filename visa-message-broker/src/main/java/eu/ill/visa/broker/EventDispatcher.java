package eu.ill.visa.broker;

import eu.ill.visa.broker.domain.messages.BroadcastEventMessage;
import eu.ill.visa.broker.domain.messages.ClientEventCarrierMessage;
import eu.ill.visa.broker.domain.messages.EventForClientMessage;
import eu.ill.visa.broker.domain.messages.EventForUserMessage;
import eu.ill.visa.broker.domain.models.*;
import eu.ill.visa.broker.gateway.ClientEventsGateway;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@Startup
@ApplicationScoped
public class EventDispatcher {

    private final MessageBroker messageBroker;
    private final ClientEventsGateway clientEventsGateway;

    private final List<EventChannelSubscription> subscriptions = new ArrayList<>();

    @Inject
    public EventDispatcher(final jakarta.enterprise.inject.Instance<MessageBroker> messageBrokerInstance,
                           final ClientEventsGateway clientEventsGateway) {
        this.messageBroker = messageBrokerInstance.get();
        this.clientEventsGateway = clientEventsGateway;

        this.messageBroker.subscribe(EventForUserMessage.class).next(this::onEventForUser);
        this.messageBroker.subscribe(EventForClientMessage.class).next(this::onEventForClient);
        this.messageBroker.subscribe(BroadcastEventMessage.class).next(this::onBroadcastEvent);
        this.messageBroker.subscribe(ClientEventCarrierMessage.class).next(this::onEventReceivedFromClient);
    }

    public EventChannelSubscription subscribe(final String clientId, final String userId, final EventHandler eventHandler) {
        final EventChannelSubscription subscription = new EventChannelSubscription(clientId, userId, eventHandler);
        this.subscriptions.add(subscription);
        return subscription;
    }

    public void unsubscribe(final EventChannelSubscription subscription) {
        this.subscriptions.remove(subscription);
    }

    public void forwardEventFromClient(final String clientId, final ClientEventCarrier clientEventCarrier) {
        this.messageBroker.broadcast(new ClientEventCarrierMessage(clientId, clientEventCarrier));
    }

    public void sendEventToUser(final String userId, String type) {
        this.sendEventToUser(userId, type, null);
    }

    public void sendEventToUser(final String userId, String type, Object event) {
        this.messageBroker.broadcast(new EventForUserMessage(userId, new ClientEventCarrier(type, event)));
    }

    public void sendEventToClient(final String clientId, String type) {
        this.sendEventToClient(clientId, type, null);
    }

    public void sendEventToClient(final String clientId, String type, Object event) {
        this.messageBroker.broadcast(new EventForClientMessage(clientId, new ClientEventCarrier(type, event)));
    }

    public void broadcastEvent(String type) {
        this.broadcastEvent(type, null);
    }

    public void broadcastEvent(String type, Object event) {
        this.messageBroker.broadcast(new BroadcastEventMessage(new ClientEventCarrier(type, event)));
    }

    private void onEventForUser(final EventForUserMessage message) {
        this.subscriptions.stream()
            .filter(subscription -> subscription.userId().equals(message.userId()))
            .forEach(subscriber -> subscriber.onEvent(message.event()));
    }

    private void onEventForClient(final EventForClientMessage message) {
        this.subscriptions.stream()
            .filter(subscription -> subscription.clientId().equals(message.clientId()))
            .forEach(subscriber -> subscriber.onEvent(message.event()));
    }

    private void onBroadcastEvent(final BroadcastEventMessage message) {
        this.subscriptions.forEach(subscription -> subscription.onEvent(message.event()));
    }

    private void onEventReceivedFromClient(final ClientEventCarrierMessage clientEventCarrierMessage) {
        this.clientEventsGateway.onEvent(clientEventCarrierMessage.clientId(), clientEventCarrierMessage.event());
    }

}
