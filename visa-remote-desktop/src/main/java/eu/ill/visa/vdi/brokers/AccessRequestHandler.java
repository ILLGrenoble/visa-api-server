package eu.ill.visa.vdi.brokers;

import eu.ill.visa.vdi.domain.models.ConnectedUser;

public interface AccessRequestHandler {
    void onAccessRequested(Long instanceId, ConnectedUser user, String requesterConnectionId);
}
