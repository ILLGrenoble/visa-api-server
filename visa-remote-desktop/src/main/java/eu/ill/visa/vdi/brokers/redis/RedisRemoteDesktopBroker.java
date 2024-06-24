package eu.ill.visa.vdi.brokers.redis;

import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.brokers.RemoteDesktopMessageSubscriptionList;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.runtime.Shutdown;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LookupIfProperty(name = "vdi.redisEnabled", stringValue = "true")
@ApplicationScoped
public class RedisRemoteDesktopBroker implements RemoteDesktopBroker {
    private static final Logger logger = LoggerFactory.getLogger(RedisRemoteDesktopBroker.class);

    private final RedisRemoteDesktopPubSub redisPubSub;

    public RedisRemoteDesktopBroker(final RedisDataSource redisDataSource,
                                    final VirtualDesktopConfiguration configuration) {
        this.redisPubSub = new RedisRemoteDesktopPubSub(redisDataSource);
        logger.info("Enabling load-balanced web-sockets with redis at {}, using db {}", configuration.redisURL().get(), configuration.redisDatabase());
    }

    @Shutdown
    public void shutdown() {
        try {
            this.redisPubSub.shutdown();

        } catch (Exception e) {
            logger.warn("Failed to shutdown Redis pubSubs: {}", e.getMessage());
        }
    }

    public <T> RemoteDesktopMessageSubscriptionList<T> subscribe(Class<T> clazz) {
        return this.redisPubSub.subscribe(clazz);
    }

    public <T> void broadcast(T message) {
        this.redisPubSub.broadcast(message);
    }
}
