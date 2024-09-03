package eu.ill.visa.business.gateway.events;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.enumerations.InstanceState;

import java.util.Date;
import java.util.List;

public record InstanceStateChangedEvent(Long instanceId,
                                        String instanceUid,
                                        String name,
                                        String comments,
                                        InstanceState state,
                                        String ipAddress,
                                        Date terminationDate,
                                        Date expirationDate,
                                        Boolean deleteRequested,
                                        Boolean unrestrictedMemberAccess,
                                        List<String> activeProtocols) {


    public InstanceStateChangedEvent(final Instance instance) {
        this(instance.getId(),
            instance.getUid(),
            instance.getName(),
            instance.getComments(),
            instance.getState(),
            instance.getIpAddress(),
            instance.getTerminationDate(),
            instance.getExpirationDate(),
            instance.getDeleteRequested(),
            instance.canAccessWhenOwnerAway(),
            instance.getActiveProtocols()
        );
    }
}
