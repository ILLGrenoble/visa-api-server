package eu.ill.visa.vdi.brokers.redis;

import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

@Startup
@LookupIfProperty(name = "vdi.redisEnabled", stringValue = "true")
@ApplicationScoped
public class RedisRemoteDesktopBroker implements RemoteDesktopBroker {

    private final RedisAccessRequestPubSub accessRequestPubSub;
    private final RedisAccessCancellationPubSub accessCancellationPubSub;

    public RedisRemoteDesktopBroker(final RedisDataSource redisDataSource,
                                    final DesktopAccessService desktopAccessService) {
        this.accessRequestPubSub = new RedisAccessRequestPubSub(redisDataSource, desktopAccessService);
        this.accessCancellationPubSub = new RedisAccessCancellationPubSub(redisDataSource, desktopAccessService);
    }

    @PreDestroy
    public void shutdown() {
        this.accessRequestPubSub.shutdown();
        this.accessCancellationPubSub.shutdown();
    }

    @Override
    public void onAccessRequested(Long instanceId, ConnectedUser user, String requesterConnectionId) {
        this.accessRequestPubSub.onAccessRequested(instanceId, user, requesterConnectionId);
    }

    @Override
    public void onAccessCancelled(Long instanceId, ConnectedUser requester, String requesterConnectionId) {
        this.accessCancellationPubSub.onAccessCancelled(instanceId, requester, requesterConnectionId);
    }
}
