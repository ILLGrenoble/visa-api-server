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
public class ClientEventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(ClientEventDispatcher.class);

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
    public <T> void onEvent(final SocketClient client, final ClientEventCarrier clientEventCarrier) {
        this.subscriptionLists.stream()
            .filter(clientEventSubscriptionList -> clientEventSubscriptionList.getType().equals(clientEventCarrier.type()))
            .findAny()
            .ifPresent(clientEventSubscriptionList -> {
                try {
                    final T event = (T)this.mapper.convertValue(clientEventCarrier.data(), clientEventSubscriptionList.getEventClass());
                    ((ClientEventSubscriptionList<T>)clientEventSubscriptionList).onEvent(client, event);

                } catch (Exception e) {
                    logger.error("Failed to deserialize event of type {} to class {}: {}", clientEventCarrier.type(), clientEventSubscriptionList.getEventClass(), e.getMessage());
                }
            });
    }
}
