package eu.ill.visa.vdi.brokers.local;

import eu.ill.visa.vdi.brokers.RemoteDesktopPubSub;

public class LocalRemoteDesktopPubSub<T> implements RemoteDesktopPubSub<T>{

    private final RemoteDesktopMessageHandler<T> handler;

    public LocalRemoteDesktopPubSub(final RemoteDesktopMessageHandler<T> handler) {
        this.handler = handler;
    }

    public void broadcast(T message) {
        this.handler.onMessage(message);
    }

}
