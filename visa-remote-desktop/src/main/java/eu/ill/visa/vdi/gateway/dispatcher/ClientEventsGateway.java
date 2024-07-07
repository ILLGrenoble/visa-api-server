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

    private final List<ClientConnectSubscriber> connectSubscriptionList = new ArrayList<>();
    private final List<ClientDisconnectSubscriber> disconnectSubscriptionList = new ArrayList<>();
    private final List<ClientEventSubscriptionList<?>> subscriptionLists = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();


    public void addConnectSubscriber(final ClientConnectSubscriber connectSubscriber) {
        this.connectSubscriptionList.add(connectSubscriber);
    }

    public void addDisconnectSubscriber(final ClientDisconnectSubscriber disconnectSubscriber) {
        this.disconnectSubscriptionList.add(disconnectSubscriber);
    }

    @SuppressWarnings("unchecked")
    public <T> ClientEventSubscriptionList<T> subscribe(final String type, Class<T> eventClass) {
        ClientEventSubscriptionList<?> clientEventSubscriptionList = this.subscriptionLists.stream().filter(subscriptionList -> subscriptionList.getType().equals(type)).findFirst().orElse(null);
        if (clientEventSubscriptionList == null) {
            clientEventSubscriptionList = new ClientEventSubscriptionList<T>(type, eventClass);
            this.subscriptionLists.add(clientEventSubscriptionList);

        } else {
            if (!clientEventSubscriptionList.getEventClass().equals(eventClass)) {
                throw new RuntimeException(String.format("Client event subscription list for type %s already exists for class %s", type, clientEventSubscriptionList.getEventClass()));
            }
        }
        return (ClientEventSubscriptionList<T>)clientEventSubscriptionList;
    }

    public void onConnect(final SocketClient client) {
        this.connectSubscriptionList.forEach(connectSubscriber -> connectSubscriber.onConnect(client));
    }

    public void onDisconnect(final SocketClient client) {
        this.disconnectSubscriptionList.forEach(disconnectSubscriber -> disconnectSubscriber.onDisconnect(client));
    }

    @SuppressWarnings("unchecked")
    public <T> void onEvent(final SocketClient client, final ClientEventCarrier clientEventCarrier) {
        this.subscriptionLists.stream()
            .filter(clientEventSubscriptionList -> clientEventSubscriptionList.getType().equals(clientEventCarrier.type()))
            .findAny()
            .ifPresent(clientEventSubscriptionList -> {
                final Class<?> eventClass = clientEventSubscriptionList.getEventClass();
                if (eventClass.equals(String.class)) {
                    ((ClientEventSubscriptionList<String>)clientEventSubscriptionList).onEvent(client, clientEventCarrier.data().toString());

                } else if (eventClass.equals(Integer.class)) {
                    ((ClientEventSubscriptionList<Integer>)clientEventSubscriptionList).onEvent(client, Integer.parseInt(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Long.class)) {
                    ((ClientEventSubscriptionList<Long>)clientEventSubscriptionList).onEvent(client, Long.parseLong(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Float.class)) {
                    ((ClientEventSubscriptionList<Float>)clientEventSubscriptionList).onEvent(client, Float.parseFloat(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Double.class)) {
                    ((ClientEventSubscriptionList<Double>)clientEventSubscriptionList).onEvent(client, Double.parseDouble(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Boolean.class)) {
                    ((ClientEventSubscriptionList<Boolean>)clientEventSubscriptionList).onEvent(client, Boolean.parseBoolean(clientEventCarrier.data().toString()));

                } else {
                    try {
                        final T event = (T)this.mapper.convertValue(clientEventCarrier.data(), clientEventSubscriptionList.getEventClass());
                        ((ClientEventSubscriptionList<T>)clientEventSubscriptionList).onEvent(client, event);

                    } catch (Exception e) {
                        logger.error("Failed to deserialize event of type {} to class {}: {}", clientEventCarrier.type(), clientEventSubscriptionList.getEventClass(), e.getMessage());
                    }
                }
            });
    }
}
