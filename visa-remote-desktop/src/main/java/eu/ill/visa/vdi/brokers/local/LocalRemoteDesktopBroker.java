package eu.ill.visa.vdi.brokers.local;

import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.brokers.RemoteDesktopPubSub;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;

@LookupIfProperty(name = "vdi.redisEnabled", stringValue = "false")
@ApplicationScoped
public class LocalRemoteDesktopBroker implements RemoteDesktopBroker {

    @Override
    public <T> RemoteDesktopPubSub<T> createPubSub(Class<T> clazz, RemoteDesktopPubSub.RemoteDesktopMessageHandler<T> handler) {
        return new LocalRemoteDesktopPubSub<>(handler);
    }
}
