package eu.ill.visa.vdi.brokers.redis;

import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@Startup
@LookupIfProperty(name = "vdi.redisEnabled", stringValue = "true")
@ApplicationScoped
public class RedisRemoteDesktopBroker implements RemoteDesktopBroker {

    private final RedisDataSource redisDataSource;

    private final List<RedisRemoteDesktopPubSub<?>> pubSubs = new ArrayList<>();

    public RedisRemoteDesktopBroker(final RedisDataSource redisDataSource) {
        this.redisDataSource = redisDataSource;
    }

    @PreDestroy
    public void shutdown() {
        this.pubSubs.forEach(RedisRemoteDesktopPubSub::shutdown);
    }

    public <T> RedisRemoteDesktopPubSub<T> createPubSub(Class<T> clazz, final RedisRemoteDesktopPubSub.RemoteDesktopMessageHandler<T> handler) {
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
