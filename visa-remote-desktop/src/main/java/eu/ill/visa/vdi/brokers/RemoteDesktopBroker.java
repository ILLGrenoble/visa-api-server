package eu.ill.visa.vdi.brokers;

public interface RemoteDesktopBroker {
    <T> RemoteDesktopMessageSubscriptionList<T> subscribe(Class<T> clazz);
    <T> void broadcast(T message);
}
