package eu.ill.visa.vdi.brokers.local;

import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.brokers.RemoteDesktopMessageSubscriptionList;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@LookupIfProperty(name = "vdi.redisEnabled", stringValue = "false")
@ApplicationScoped
public class LocalRemoteDesktopBroker implements RemoteDesktopBroker {
    private static final Logger logger = LoggerFactory.getLogger(LocalRemoteDesktopBroker.class);

    private final List<RemoteDesktopMessageSubscriptionList<?>> subscriptionLists = new ArrayList<>();

    public LocalRemoteDesktopBroker() {
        logger.info("Enabling single server web-sockets");
    }

    @Override
    public synchronized <T> RemoteDesktopMessageSubscriptionList<T> subscribe(Class<T> clazz) {
        RemoteDesktopMessageSubscriptionList<T> subscriptionList = this.getSubscriptionList(clazz.getName());
        if (subscriptionList == null) {
            subscriptionList = new RemoteDesktopMessageSubscriptionList<T>(clazz);
            this.subscriptionLists.add(subscriptionList);
        }
        return subscriptionList;
    }

    @Override
    public synchronized <T> void broadcast(T message) {
        RemoteDesktopMessageSubscriptionList<T> subscriptionList = this.getSubscriptionList(message.getClass().getName());
        if (subscriptionList != null) {
            subscriptionList.onMessage(message);
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> RemoteDesktopMessageSubscriptionList<T> getSubscriptionList(final String className) {
        return (RemoteDesktopMessageSubscriptionList<T>) this.subscriptionLists.stream()
            .filter(remoteDesktopMessageSubscriptionList -> remoteDesktopMessageSubscriptionList.getClazzName().equals(className))
            .findAny()
            .orElse(null);
    }
}
