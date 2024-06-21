package eu.ill.visa.vdi.brokers;

import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.Role;

public interface RemoteDesktopBroker {
    void onAccessRequested(Long instanceId, ConnectedUser user, String requesterConnectionId);
    void onAccessRequestCancelled(Long instanceId, ConnectedUser user, String requesterConnectionId);
    void onAccessRequestResponse(Long instanceId, String requesterConnectionId, Role role);
}
