package eu.ill.visa.vdi.brokers.redis;

import eu.ill.visa.vdi.brokers.RemoteDesktopPubSub;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import java.util.function.Consumer;

public class RedisRemoteDesktopPubSub<T> implements RemoteDesktopPubSub<T>, Consumer<T> {

    private final String channel;
    private final RemoteDesktopMessageHandler<T> handler;

    private final PubSubCommands.RedisSubscriber subscriber;
    private final PubSubCommands<T> publisher;

    public RedisRemoteDesktopPubSub(final RedisDataSource redisDataSource,
                                    final Class<T> clazz,
                                    final RemoteDesktopMessageHandler<T> handler) {
        this.handler = handler;
        this.channel =  clazz.getName();
        this.publisher = redisDataSource.pubsub(clazz);
        this.subscriber = this.publisher.subscribe(this.channel, this);
    }

    public void shutdown() {
        this.subscriber.unsubscribe();
    }

    @Override
    public void accept(T message) {
        // Execute handler on worker thread to allow JTA transactions
        Uni.createFrom()
            .item(message)
            .emitOn(Infrastructure.getDefaultWorkerPool())
            .subscribe()
            .with(this.handler::onMessage);
    }

    public void broadcast(T message) {
        this.publisher.publish(this.channel, message);
    }

}
