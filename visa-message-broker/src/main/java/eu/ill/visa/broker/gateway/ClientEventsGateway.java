package eu.ill.visa.broker.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.visa.broker.domain.models.ClientEventCarrier;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ClientEventsGateway {

    private static final Logger logger = LoggerFactory.getLogger(ClientEventsGateway.class);

    private final List<ClientEventSubscriptionList<?>> subscriptionLists = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();

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


    @SuppressWarnings("unchecked")
    public <T> void onEvent(final String clientId, final ClientEventCarrier clientEventCarrier) {
        this.subscriptionLists.stream()
            .filter(socketEventSubscriptionList -> socketEventSubscriptionList.getType().equals(clientEventCarrier.type()))
            .findAny()
            .ifPresent(socketEventSubscriptionList -> {
                final Class<?> eventClass = socketEventSubscriptionList.getEventClass();
                if (eventClass.equals(String.class)) {
                    ((ClientEventSubscriptionList<String>)socketEventSubscriptionList).onEvent(clientId, clientEventCarrier.data().toString());

                } else if (eventClass.equals(Integer.class)) {
                    ((ClientEventSubscriptionList<Integer>)socketEventSubscriptionList).onEvent(clientId, Integer.parseInt(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Long.class)) {
                    ((ClientEventSubscriptionList<Long>)socketEventSubscriptionList).onEvent(clientId, Long.parseLong(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Float.class)) {
                    ((ClientEventSubscriptionList<Float>)socketEventSubscriptionList).onEvent(clientId, Float.parseFloat(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Double.class)) {
                    ((ClientEventSubscriptionList<Double>)socketEventSubscriptionList).onEvent(clientId, Double.parseDouble(clientEventCarrier.data().toString()));

                } else if (eventClass.equals(Boolean.class)) {
                    ((ClientEventSubscriptionList<Boolean>)socketEventSubscriptionList).onEvent(clientId, Boolean.parseBoolean(clientEventCarrier.data().toString()));

                } else {
                    try {
                        final T event = (T)this.mapper.convertValue(clientEventCarrier.data(), socketEventSubscriptionList.getEventClass());
                        ((ClientEventSubscriptionList<T>)socketEventSubscriptionList).onEvent(clientId, event);

                    } catch (Exception e) {
                        logger.error("Failed to deserialize event of type {} to class {}: {}", clientEventCarrier.type(), socketEventSubscriptionList.getEventClass(), e.getMessage());
                    }
                }
            });
    }
}
