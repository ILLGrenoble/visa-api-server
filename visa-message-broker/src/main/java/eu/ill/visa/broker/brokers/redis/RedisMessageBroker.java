package eu.ill.visa.broker.brokers.redis;

import eu.ill.visa.broker.MessageBroker;
import eu.ill.visa.broker.MessageBrokerConfiguration;
import eu.ill.visa.broker.MessageSubscriptionList;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LookupIfProperty(name = "broker.redisEnabled", stringValue = "true")
@Singleton
public class RedisMessageBroker implements MessageBroker {
    private static final Logger logger = LoggerFactory.getLogger(RedisMessageBroker.class);

    private final RedisMessageBrokerPubSub redisPubSub;

    public RedisMessageBroker(final RedisDataSource redisDataSource,
                              final MessageBrokerConfiguration configuration) {
        this.redisPubSub = new RedisMessageBrokerPubSub(redisDataSource);
        logger.info("Enabling redis message broker at {}, using db {}", configuration.redisURL().get(), configuration.redisDatabase());
    }

    @Override
    public void shutdown() {
        try {
            this.redisPubSub.shutdown();

        } catch (Exception e) {
            logger.warn("Failed to shutdown Redis pubSubs: {}", e.getMessage());
        }
    }

    @Override
    public <T> MessageSubscriptionList<T> subscribe(Class<T> clazz) {
        return this.redisPubSub.subscribe(clazz);
    }

    @Override
    public <T> void broadcast(T message) {
        this.redisPubSub.broadcast(message);
    }
}
