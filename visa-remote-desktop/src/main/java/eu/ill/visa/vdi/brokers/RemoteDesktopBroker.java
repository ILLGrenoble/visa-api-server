package eu.ill.visa.vdi.brokers;

import eu.ill.visa.vdi.brokers.redis.RedisRemoteDesktopPubSub;

public interface RemoteDesktopBroker {
    <T> RemoteDesktopPubSub<T> createPubSub(Class<T> clazz, final RedisRemoteDesktopPubSub.RemoteDesktopMessageHandler<T> handler);
}
