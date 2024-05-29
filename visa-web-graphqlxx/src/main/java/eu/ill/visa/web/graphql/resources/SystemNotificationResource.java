package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.ClientNotificationService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.types.SystemNotificationType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class SystemNotificationResource {

    private final ClientNotificationService clientNotificationService;

    @Inject
    public SystemNotificationResource(final ClientNotificationService clientNotificationService) {
        this.clientNotificationService = clientNotificationService;
    }

    @Query
    public @NotNull List<SystemNotificationType> systemNotifications() throws DataFetchingException {
        try {
            return clientNotificationService.getAllSystemNotifications().stream()
                .map(SystemNotificationType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

}
