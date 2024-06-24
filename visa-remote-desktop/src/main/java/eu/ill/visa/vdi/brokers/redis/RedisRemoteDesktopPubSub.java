package eu.ill.visa.vdi.brokers.redis;

import eu.ill.visa.vdi.brokers.RemoteDesktopMessageSubscriptionList;
import eu.ill.visa.vdi.domain.exceptions.MessageMarshallingException;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RedisRemoteDesktopPubSub implements Consumer<RedisMessageCarrier> {

    private static final Logger logger = LoggerFactory.getLogger(RedisRemoteDesktopPubSub.class);

    private final static String CHANNEL = "REMOTE_DESKTOP_MESSAGE";

    private final PubSubCommands.RedisSubscriber subscriber;
    private final PubSubCommands<RedisMessageCarrier> publisher;

    private final List<RemoteDesktopMessageSubscriptionList<?>> subscriptionLists = new ArrayList<>();

    public RedisRemoteDesktopPubSub(final RedisDataSource redisDataSource) {
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

    public synchronized <T> RemoteDesktopMessageSubscriptionList<T> subscribe(Class<T> clazz) {
        RemoteDesktopMessageSubscriptionList<T> subscriptionList = this.getSubscriptionList(clazz.getName());
        if (subscriptionList == null) {
            subscriptionList = new RemoteDesktopMessageSubscriptionList<T>(clazz);
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
            RemoteDesktopMessageSubscriptionList<T> subscriptionList = this.getSubscriptionList(message.getClassName());
            subscriptionList.onMessage(data);

        } catch (MessageMarshallingException e) {
            logger.error("Failed to deserialize remote desktop message payload: e");
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> RemoteDesktopMessageSubscriptionList<T> getSubscriptionList(final String className) {
        return (RemoteDesktopMessageSubscriptionList<T>) this.subscriptionLists.stream()
            .filter(remoteDesktopMessageSubscriptionList -> remoteDesktopMessageSubscriptionList.getClazzName().equals(className))
            .findAny()
            .orElse(null);
    }

}
