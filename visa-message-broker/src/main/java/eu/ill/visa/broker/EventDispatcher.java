package eu.ill.visa.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.visa.broker.domain.exceptions.MessageMarshallingException;
import eu.ill.visa.broker.domain.messages.*;
import eu.ill.visa.broker.domain.models.ClientEventCarrier;
import eu.ill.visa.broker.domain.models.EventChannelSubscription;
import eu.ill.visa.broker.domain.models.EventHandler;
import eu.ill.visa.broker.gateway.ClientEventsGateway;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Startup
@ApplicationScoped
public class EventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(EventDispatcher.class);

    private final MessageBroker messageBroker;
    private final ClientEventsGateway clientEventsGateway;
    private final ObjectMapper mapper = new ObjectMapper();

    private final List<EventChannelSubscription> subscriptions = new ArrayList<>();

    @Inject
    public EventDispatcher(final jakarta.enterprise.inject.Instance<MessageBroker> messageBrokerInstance,
                           final ClientEventsGateway clientEventsGateway) {
        this.messageBroker = messageBrokerInstance.get();
        this.clientEventsGateway = clientEventsGateway;

        this.messageBroker.subscribe(EventForUserMessage.class).next(this::onEventForUser);
        this.messageBroker.subscribe(EventForRoleMessage.class).next(this::onEventForRole);
        this.messageBroker.subscribe(EventForClientMessage.class).next(this::onEventForClient);
        this.messageBroker.subscribe(BroadcastEventMessage.class).next(this::onBroadcastEvent);
        this.messageBroker.subscribe(ClientEventCarrierMessage.class).next(this::onEventReceivedFromClient);
    }

    public EventChannelSubscription subscribe(final String clientId, final String userId, final List<String> roles, final EventHandler eventHandler) {
        final EventChannelSubscription subscription = new EventChannelSubscription(clientId, userId, roles, eventHandler);
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

    public void sendEventForRole(String role, String type) {
        this.sendEventForRole(role, type, null);
    }

    public void sendEventForRole(String role, String type, Object event) {
        this.messageBroker.broadcast(new EventForRoleMessage(role, new ClientEventCarrier(type, event)));
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
        try {
            this.subscriptions.stream()
                .filter(subscription -> subscription.userId().equals(message.userId()))
                .forEach(subscriber -> subscriber.onEvent(this.deserializeClientEventCarrier(message.event())));

        } catch (MessageMarshallingException e) {
            logger.error(e.getMessage());
        }
    }

    private void onEventForRole(final EventForRoleMessage message) {
        try {
            this.subscriptions.stream()
                .filter(subscription -> subscription.roles().contains(message.role()))
                .forEach(subscriber -> subscriber.onEvent(this.deserializeClientEventCarrier(message.event())));

        } catch (MessageMarshallingException e) {
            logger.error(e.getMessage());
        }
    }

    private void onEventForClient(final EventForClientMessage message) {
        try {
            this.subscriptions.stream()
                .filter(subscription -> subscription.clientId().equals(message.clientId()))
                .forEach(subscriber -> subscriber.onEvent(this.deserializeClientEventCarrier(message.event())));

        } catch (MessageMarshallingException e) {
            logger.error(e.getMessage());
        }
    }

    private void onBroadcastEvent(final BroadcastEventMessage message) {
        try {
            this.subscriptions.forEach(subscription -> subscription.onEvent(this.deserializeClientEventCarrier(message.event())));

        } catch (MessageMarshallingException e) {
            logger.error(e.getMessage());
        }
    }

    private void onEventReceivedFromClient(final ClientEventCarrierMessage clientEventCarrierMessage) {
        this.clientEventsGateway.onEvent(clientEventCarrierMessage.clientId(), clientEventCarrierMessage.event());
    }

    private ClientEventCarrier deserializeClientEventCarrier(final Object message) throws MessageMarshallingException {
        try {
            // Need to recreate the message if it has gone through redis and been serialised
            if (message instanceof ClientEventCarrier) {
                return (ClientEventCarrier) message;

            } else {
                return this.mapper.convertValue(message, ClientEventCarrier.class);
            }

        } catch (Exception e) {
            throw new MessageMarshallingException(String.format("Failed to deserialize ClientEventCarrier from message of type %s: %s", message.getClass().getName(), e.getMessage()));
        }
    }
}
