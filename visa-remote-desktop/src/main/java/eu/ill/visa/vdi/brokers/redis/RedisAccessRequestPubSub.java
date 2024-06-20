package eu.ill.visa.vdi.brokers.redis;

import eu.ill.visa.vdi.brokers.AccessRequestHandler;
import eu.ill.visa.vdi.brokers.redis.messages.AccessRequestMessage;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;

import java.util.function.Consumer;

public class RedisAccessRequestPubSub implements AccessRequestHandler, Consumer<AccessRequestMessage> {

    private static final String CHANNEL = AccessRequestMessage.class.getName();
    private final DesktopAccessService desktopAccessService;

    private final PubSubCommands.RedisSubscriber subscriber;
    private final PubSubCommands<AccessRequestMessage> publisher;

    public RedisAccessRequestPubSub(final RedisDataSource redisDataSource,
                                    final DesktopAccessService desktopAccessService) {
        this.desktopAccessService = desktopAccessService;
        this.publisher = redisDataSource.pubsub(AccessRequestMessage.class);
        this.subscriber = this.publisher.subscribe(CHANNEL, this);
    }

    public void shutdown() {
        this.subscriber.unsubscribe();
    }

    @Override
    public void accept(AccessRequestMessage message) {
        this.desktopAccessService.onAccessRequested(message.instanceId(), message.user(), message.requesterConnectionId());
    }

    public void onAccessRequested(Long instanceId, ConnectedUser requester, String requesterConnectionId) {
        this.publisher.publish(CHANNEL, new AccessRequestMessage(instanceId, requester, requesterConnectionId));
    }
}
