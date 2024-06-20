package eu.ill.visa.vdi.brokers.redis;

import eu.ill.visa.vdi.brokers.AccessCancellationHandler;
import eu.ill.visa.vdi.brokers.redis.messages.AccessRequestCancellationMessage;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;

import java.util.function.Consumer;

public class RedisAccessCancellationPubSub implements AccessCancellationHandler, Consumer<AccessRequestCancellationMessage> {

    private static final String CHANNEL = AccessRequestCancellationMessage.class.getName();

    private final DesktopAccessService desktopAccessService;

    private final PubSubCommands.RedisSubscriber subscriber;
    private final PubSubCommands<AccessRequestCancellationMessage> publisher;

    public RedisAccessCancellationPubSub(final RedisDataSource redisDataSource,
                                         final DesktopAccessService desktopAccessService) {
        this.desktopAccessService = desktopAccessService;
        this.publisher = redisDataSource.pubsub(AccessRequestCancellationMessage.class);
        this.subscriber = this.publisher.subscribe(CHANNEL, this);
    }

    public void shutdown() {
        this.subscriber.unsubscribe();
    }

    @Override
    public void accept(AccessRequestCancellationMessage message) {
        this.desktopAccessService.onAccessRequestCancelled(message.instanceId(), message.user(), message.requesterConnectionId());
    }

    public void onAccessCancelled(Long instanceId, ConnectedUser requester, String requesterConnectionId) {
        this.publisher.publish(CHANNEL, new AccessRequestCancellationMessage(instanceId, requester, requesterConnectionId));
    }
}
