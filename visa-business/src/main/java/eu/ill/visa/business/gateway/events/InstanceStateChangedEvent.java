package eu.ill.visa.business.gateway.events;

import eu.ill.visa.core.entity.ImageProtocol;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Date;
import java.util.List;

@RegisterForReflection
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
                                        List<String> activeProtocols,
                                        ImageProtocol vdiProtocol) {


    public InstanceStateChangedEvent(final Instance instance) {
        this(instance.getId(),
            instance.getUid(),
            instance.getName(),
            instance.getComments(),
            instance.getState(),
            instance.getIpAddress(),
            instance.getTerminationDate(),
            instance.getExpirationDate() != null ? instance.getExpirationDate() : instance.getTerminationDate(),
            instance.getDeleteRequested(),
            instance.canAccessWhenOwnerAway(),
            instance.getActiveProtocols(),
            instance.getVdiProtocol()
        );
    }
}
