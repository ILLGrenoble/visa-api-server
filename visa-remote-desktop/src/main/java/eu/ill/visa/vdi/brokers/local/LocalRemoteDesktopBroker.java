package eu.ill.visa.vdi.brokers.local;

import eu.ill.visa.vdi.brokers.RemoteDesktopBroker;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;

@LookupIfProperty(name = "vdi.redisEnabled", stringValue = "false")
@ApplicationScoped
public class LocalRemoteDesktopBroker implements RemoteDesktopBroker {

    private final DesktopAccessService desktopAccessService;

    public LocalRemoteDesktopBroker(final DesktopAccessService desktopAccessService) {
        this.desktopAccessService = desktopAccessService;
    }

    @Override
    public void onAccessRequested(Long instanceId, ConnectedUser requester, String requesterConnectionId) {
        this.desktopAccessService.onAccessRequested(instanceId, requester, requesterConnectionId);
    }

    @Override
    public void onAccessCancelled(Long instanceId, ConnectedUser requester, String requesterConnectionId) {
        this.desktopAccessService.onAccessRequestCancelled(instanceId, requester, requesterConnectionId);
    }
}
