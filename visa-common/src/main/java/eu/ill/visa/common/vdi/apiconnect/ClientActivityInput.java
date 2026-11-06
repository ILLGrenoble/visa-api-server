package eu.ill.visa.common.vdi.apiconnect;


import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.enums.InstanceActivityType;

import java.util.Date;

public record ClientActivityInput(Long instanceId,
                                  String connectionId,
                                  String clientId,
                                  ConnectedUser user,
                                  Date lastSeenAt,
                                  Date lastInteractionAt,
                                  InstanceActivityType instanceActivityType) {
}
