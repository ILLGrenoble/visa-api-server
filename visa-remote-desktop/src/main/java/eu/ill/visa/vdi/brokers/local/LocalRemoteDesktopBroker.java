package eu.ill.visa.vdi.brokers.local;

import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.brokers.RemoteDesktopPubSub;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LookupIfProperty(name = "vdi.redisEnabled", stringValue = "false")
@ApplicationScoped
public class LocalRemoteDesktopBroker implements RemoteDesktopBroker {
    private static final Logger logger = LoggerFactory.getLogger(LocalRemoteDesktopBroker.class);

    public LocalRemoteDesktopBroker() {
        logger.info("Enabling single server web-sockets");
    }

    @Override
    public <T> RemoteDesktopPubSub<T> createPubSub(Class<T> clazz, RemoteDesktopPubSub.RemoteDesktopMessageHandler<T> handler) {
        return new LocalRemoteDesktopPubSub<>(handler);
    }
}
