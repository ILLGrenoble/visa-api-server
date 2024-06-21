package eu.ill.visa.vdi.brokers.redis;

import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.brokers.redis.messages.AccessRequestCancellationMessage;
import eu.ill.visa.vdi.brokers.redis.messages.AccessRequestMessage;
import eu.ill.visa.vdi.brokers.redis.messages.AccessRequestResponseMessage;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.Role;
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

    private final RemoteDesktopPubSub<AccessRequestMessage> accessRequestPubSub;
    private final RemoteDesktopPubSub<AccessRequestCancellationMessage> accessCancellationPubSub;
    private final RemoteDesktopPubSub<AccessRequestResponseMessage> accessRequestResponsePubSub;

    private final List<RemoteDesktopPubSub<?>> pubSubs = new ArrayList<>();

    public RedisRemoteDesktopBroker(final RedisDataSource redisDataSource,
                                    final DesktopAccessService desktopAccessService) {
        this.accessRequestPubSub = this.createPubSub(redisDataSource, AccessRequestMessage.class, (message -> {
            desktopAccessService.onAccessRequested(message.instanceId(), message.user(), message.requesterConnectionId());
        }));

        this.accessCancellationPubSub = this.createPubSub(redisDataSource, AccessRequestCancellationMessage.class, (message -> {
            desktopAccessService.onAccessRequestCancelled(message.instanceId(), message.user(), message.requesterConnectionId());
        }));

        this.accessRequestResponsePubSub = this.createPubSub(redisDataSource, AccessRequestResponseMessage.class, (message -> {
            desktopAccessService.onAccessRequestResponse(message.instanceId(), message.requesterConnectionId(), message.role());
        }));
    }

    @PreDestroy
    public void shutdown() {
        this.pubSubs.forEach(RemoteDesktopPubSub::shutdown);
    }

    @Override
    public void onAccessRequested(Long instanceId, ConnectedUser user, String requesterConnectionId) {
        this.accessRequestPubSub.broadcast(new AccessRequestMessage(instanceId, user, requesterConnectionId));
    }

    @Override
    public void onAccessRequestCancelled(Long instanceId, ConnectedUser requester, String requesterConnectionId) {
        this.accessCancellationPubSub.broadcast(new AccessRequestCancellationMessage(instanceId, requester, requesterConnectionId));
    }

    @Override
    public void onAccessRequestResponse(Long instanceId, String requesterConnectionId, Role role) {
        this.accessRequestResponsePubSub.broadcast(new AccessRequestResponseMessage(instanceId, requesterConnectionId, role));
    }


    private <T> RemoteDesktopPubSub<T> createPubSub(final RedisDataSource redisDataSource,
                                                    final Class<T> clazz,
                                                    final RemoteDesktopPubSub.RemoteDesktopMessageHandler<T> handler) {
        final RemoteDesktopPubSub<T> pubSub = new RemoteDesktopPubSub<>(redisDataSource, clazz, handler);
        this.pubSubs.add(pubSub);
        return pubSub;
    }
}
