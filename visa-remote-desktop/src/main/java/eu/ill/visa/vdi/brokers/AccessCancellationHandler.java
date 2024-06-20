package eu.ill.visa.vdi.brokers;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public interface AccessCancellationHandler {
    void onAccessCancelled(Long instanceId, ConnectedUser user, String requesterConnectionId);
}
