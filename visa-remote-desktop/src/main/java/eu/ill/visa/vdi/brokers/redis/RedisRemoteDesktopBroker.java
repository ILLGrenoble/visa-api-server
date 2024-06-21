package eu.ill.visa.vdi.brokers.redis;

import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.runtime.Shutdown;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@LookupIfProperty(name = "vdi.redisEnabled", stringValue = "true")
@ApplicationScoped
public class RedisRemoteDesktopBroker implements RemoteDesktopBroker {
    private static final Logger logger = LoggerFactory.getLogger(RedisRemoteDesktopBroker.class);

    private final RedisDataSource redisDataSource;

    private final List<RedisRemoteDesktopPubSub<?>> pubSubs = new ArrayList<>();

    public RedisRemoteDesktopBroker(final RedisDataSource redisDataSource,
                                    final VirtualDesktopConfiguration configuration) {
        this.redisDataSource = redisDataSource;
        logger.info("Enabling load-balanced web-sockets with redis at {}, using db {}", configuration.redisURL().get(), configuration.redisDatabase());
    }

    @Shutdown
    public void shutdown() {
        try {
            this.pubSubs.forEach(RedisRemoteDesktopPubSub::shutdown);

        } catch (Exception e) {
            logger.warn("Failed to shutdown all pubSubs: {}", e.getMessage());
        }
    }

    public synchronized <T> RedisRemoteDesktopPubSub<T> createPubSub(Class<T> clazz, final RedisRemoteDesktopPubSub.RemoteDesktopMessageHandler<T> handler) {
        return this.createPubSub(this.redisDataSource, clazz, handler);
    }

    private <T> RedisRemoteDesktopPubSub<T> createPubSub(final RedisDataSource redisDataSource,
                                                         final Class<T> clazz,
                                                         final RedisRemoteDesktopPubSub.RemoteDesktopMessageHandler<T> handler) {
        final RedisRemoteDesktopPubSub<T> pubSub = new RedisRemoteDesktopPubSub<>(redisDataSource, clazz, handler);
        this.pubSubs.add(pubSub);
        return pubSub;
    }
}
