package eu.ill.visa.web.rest.controllers.vdi.dto;



import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.vdi.domain.models.ConnectedUser;

import java.util.Date;

public record ClientActivityInput(String connectionId,
                                  String clientId,
                                  ConnectedUser user,
                                  Date lastSeenAt,
                                  Date lastInteractionAt,
                                  InstanceActivityType instanceActivityType) {
}
