package eu.ill.visa.vdi.brokers;

public interface RemoteDesktopPubSub<T> {
    void broadcast(T message);

    interface RemoteDesktopMessageHandler<T> {
        void onMessage(T message);
    }
}
