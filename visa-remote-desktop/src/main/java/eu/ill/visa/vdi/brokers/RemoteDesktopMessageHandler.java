package eu.ill.visa.vdi.brokers;

public interface RemoteDesktopMessageHandler<T> {
    void onMessage(T message);
}
