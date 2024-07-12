package eu.ill.visa.vdi.gateway.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.events.ClientEventCarrier;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ClientEventsGateway {

    private static final Logger logger = LoggerFactory.getLogger(ClientEventsGateway.class);

    private final List<SocketConnectSubscriber> connectSubscriptionList = new ArrayList<>();
    private final List<SocketDisconnectSubscriber> disconnectSubscriptionList = new ArrayList<>();
    private final List<SocketEventSubscriptionList<?>> subscriptionLists = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public void addConnectSubscriber(final SocketConnectSubscriber connectSubscriber) {
        this.connectSubscriptionList.add(connectSubscriber);
    }

    public void addDisconnectSubscriber(final SocketDisconnectSubscriber disconnectSubscriber) {
        this.disconnectSubscriptionList.add(disconnectSubscriber);
    }

    @SuppressWarnings("unchecked")
    public <T> SocketEventSubscriptionList<T> subscribe(final String type, Class<T> eventClass) {
        SocketEventSubscriptionList<?> clientEventSubscriptionList = this.subscriptionLists.stream().filter(subscriptionList -> subscriptionList.getType().equals(type)).findFirst().orElse(null);
        if (clientEventSubscriptionList == null) {
            clientEventSubscriptionList = new SocketEventSubscriptionList<T>(type, eventClass);
            this.subscriptionLists.add(clientEventSubscriptionList);

        } else {
            if (!clientEventSubscriptionList.getEventClass().equals(eventClass)) {
                throw new RuntimeException(String.format("Client event subscription list for type %s already exists for class %s", type, clientEventSubscriptionList.getEventClass()));
            }
        }
        return (SocketEventSubscriptionList<T>)clientEventSubscriptionList;
    }

    public void onConnect(final SocketClient client) {
        this.connectSubscriptionList.forEach(connectSubscriber -> connectSubscriber.onConnect(client, null));
    }

    public void onDisconnect(final SocketClient client) {
        this.disconnectSubscriptionList.forEach(disconnectSubscriber -> disconnectSubscriber.onDisconnect(client));
    }

    @SuppressWarnings("unchecked")
    public <T> void onEvent(final SocketClient client, final ClientEventCarrier clientEventCarrier) {
        this.subscriptionLists.stream()
            .filter(socketEventSubscriptionList -> socketEventSubscriptionList.getType().equals(clientEventCarrier.type()))
            .findAny()
            .ifPresent(socketEventSubscriptionList -> {
                final Class<?> eventClass = socketEventSubscriptionList.getEventClass();
                if (eventClass.equals(String.class)) {
                    ((SocketEventSubscriptionList<String>)socketEventSubscriptionList).onEvent(client, clientEventCarrier.data().toString());

                } else if (eventClass.equals(Integer.class)) {
                    ((SocketEventSubscriptionList<Integer>)socketEventSubscriptionList).onEvent(client, Integer.parseInt(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Long.class)) {
                    ((SocketEventSubscriptionList<Long>)socketEventSubscriptionList).onEvent(client, Long.parseLong(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Float.class)) {
                    ((SocketEventSubscriptionList<Float>)socketEventSubscriptionList).onEvent(client, Float.parseFloat(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Double.class)) {
                    ((SocketEventSubscriptionList<Double>)socketEventSubscriptionList).onEvent(client, Double.parseDouble(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Boolean.class)) {
                    ((SocketEventSubscriptionList<Boolean>)socketEventSubscriptionList).onEvent(client, Boolean.parseBoolean(clientEventCarrier.data().toString()));

                } else {
                    try {
                        final T event = (T)this.mapper.convertValue(clientEventCarrier.data(), socketEventSubscriptionList.getEventClass());
                        ((SocketEventSubscriptionList<T>)socketEventSubscriptionList).onEvent(client, event);

                    } catch (Exception e) {
                        logger.error("Failed to deserialize event of type {} to class {}: {}", clientEventCarrier.type(), socketEventSubscriptionList.getEventClass(), e.getMessage());
                    }
                }
            });
    }
}
