package eu.ill.visa.broker.brokers.local;

import eu.ill.visa.broker.MessageBroker;
import eu.ill.visa.broker.MessageSubscriptionList;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@LookupIfProperty(name = "broker.redisEnabled", stringValue = "false")
@Singleton
public class LocalMessageBroker implements MessageBroker {
    private static final Logger logger = LoggerFactory.getLogger(LocalMessageBroker.class);

    private final List<MessageSubscriptionList<?>> subscriptionLists = new ArrayList<>();

    public LocalMessageBroker() {
        logger.info("Enabling single server message broker");
    }

    @Override
    public void shutdown() {
    }

    public synchronized <T> MessageSubscriptionList<T> subscribe(Class<T> clazz) {
        MessageSubscriptionList<T> subscriptionList = this.getSubscriptionList(clazz.getName());
        if (subscriptionList == null) {
            subscriptionList = new MessageSubscriptionList<T>(clazz);
            this.subscriptionLists.add(subscriptionList);
        }
        return subscriptionList;
    }

    @Override
    public synchronized <T> void broadcast(T message) {
        MessageSubscriptionList<T> subscriptionList = this.getSubscriptionList(message.getClass().getName());
        if (subscriptionList != null) {
            subscriptionList.onMessage(message);
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> MessageSubscriptionList<T> getSubscriptionList(final String className) {
        return (MessageSubscriptionList<T>) this.subscriptionLists.stream()
            .filter(remoteDesktopMessageSubscriptionList -> remoteDesktopMessageSubscriptionList.getClazzName().equals(className))
            .findAny()
            .orElse(null);
    }
}
