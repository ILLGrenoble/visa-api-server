package eu.ill.visa.broker.brokers.redis;

import eu.ill.visa.broker.MessageSubscriptionList;
import eu.ill.visa.broker.domain.exceptions.MessageMarshallingException;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RedisMessageBrokerPubSub implements Consumer<RedisMessageCarrier> {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageBrokerPubSub.class);

    private final static String CHANNEL = "VISA_MESSAGE_BROKER";

    private final PubSubCommands.RedisSubscriber subscriber;
    private final PubSubCommands<RedisMessageCarrier> publisher;

    private final List<MessageSubscriptionList<?>> subscriptionLists = new ArrayList<>();

    public RedisMessageBrokerPubSub(final RedisDataSource redisDataSource) {
        this.publisher = redisDataSource.pubsub(RedisMessageCarrier.class);
        this.subscriber = this.publisher.subscribe(CHANNEL, this);
    }

    public void shutdown() {
        this.subscriber.unsubscribe();
    }

    @Override
    public void accept(RedisMessageCarrier message) {
        // Execute handler on worker thread to allow JTA transactions
        Uni.createFrom()
            .item(message)
            .emitOn(Infrastructure.getDefaultWorkerPool())
            .subscribe()
            .with(this::onMessage);
    }

    public synchronized <T> MessageSubscriptionList<T> subscribe(Class<T> clazz) {
        MessageSubscriptionList<T> subscriptionList = this.getSubscriptionList(clazz.getName());
        if (subscriptionList == null) {
            subscriptionList = new MessageSubscriptionList<T>(clazz);
            this.subscriptionLists.add(subscriptionList);
        }
        return subscriptionList;
    }

    public <T> void broadcast(T message) {
        RedisMessageCarrier remoteDesktopMessage = new RedisMessageCarrier(message);
        this.publisher.publish(CHANNEL, remoteDesktopMessage);
    }

    private <T> void onMessage(RedisMessageCarrier message) {
        try {
            T data = message.getData();
            MessageSubscriptionList<T> subscriptionList = this.getSubscriptionList(message.getClassName());
            subscriptionList.onMessage(data);

        } catch (MessageMarshallingException e) {
            logger.error("Failed to deserialize remote desktop message payload: e");
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> MessageSubscriptionList<T> getSubscriptionList(final String className) {
        return (MessageSubscriptionList<T>) this.subscriptionLists.stream()
            .filter(remoteDesktopMessageSubscriptionList -> remoteDesktopMessageSubscriptionList.getClazzName().equals(className))
            .findAny()
            .orElse(null);
    }

}
