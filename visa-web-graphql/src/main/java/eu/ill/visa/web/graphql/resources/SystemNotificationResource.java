package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.ClientNotificationService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.SystemNotification;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.SystemNotificationInput;
import eu.ill.visa.web.graphql.types.SystemNotificationType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class SystemNotificationResource {

    private final ClientNotificationService clientNotificationService;
    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");

    @Inject
    public SystemNotificationResource(final ClientNotificationService clientNotificationService) {
        this.clientNotificationService = clientNotificationService;
    }

    @Query
    public @NotNull List<SystemNotificationType> systemNotifications() {
        return clientNotificationService.getAllSystemNotifications().stream()
            .map(SystemNotificationType::new)
            .toList();
    }


    /**
     * Create a new systemNotification
     *
     * @param input the systemNotification properties
     * @return the newly created systemNotification
     */
    @Mutation
    public @NotNull SystemNotificationType createSystemNotification(@NotNull @Valid SystemNotificationInput input) throws InvalidInputException {
        final SystemNotification systemNotification = new SystemNotification();
        this.mapToSystemNotification(input, systemNotification);
        clientNotificationService.saveSystemNotification(systemNotification);
        return new SystemNotificationType(systemNotification);
    }

    /**
     * Update a new systemNotification
     *
     * @param input the systemNotification properties
     * @return the updated systemNotification
     * @throws EntityNotFoundException thrown if the systemNotification has not been found
     */
    @Mutation
    public @NotNull SystemNotificationType updateSystemNotification(@NotNull @AdaptToScalar(Scalar.Int.class) Long id, @NotNull @Valid SystemNotificationInput input) throws EntityNotFoundException, InvalidInputException {
        final SystemNotification systemNotification = this.clientNotificationService.getSystemNotificationById(id);
        if (systemNotification == null) {
            throw new EntityNotFoundException("systemNotification not found for the given id");
        }
        this.mapToSystemNotification(input, systemNotification);
        clientNotificationService.saveSystemNotification(systemNotification);
        return new SystemNotificationType(systemNotification);
    }

    /**
     * Delete a systemNotification
     *
     * @param id the instance id
     * @return a notification
     * @throws EntityNotFoundException thrown if the instance has not been found
     */
    @Mutation
    public @NotNull SystemNotificationType deleteSystemNotification(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException {
        final SystemNotification systemNotification = clientNotificationService.getSystemNotificationById(id);
        if (systemNotification == null) {
            throw new EntityNotFoundException("systemNotification not found for the given id");
        }
        clientNotificationService.deleteSystemNotification(systemNotification);
        return new SystemNotificationType(systemNotification);
    }

    private void mapToSystemNotification(SystemNotificationInput input, SystemNotification systemNotification) throws InvalidInputException {
        systemNotification.setMessage(input.getMessage());
        systemNotification.setLevel(input.getLevel());
        systemNotification.setType(input.getType());
        try {
            systemNotification.setActivatedAt(input.getActivatedAt() == null ? null : DATE_FORMAT.parse(input.getActivatedAt()));
        } catch (ParseException e) {
            throw new InvalidInputException("The activation date does not have a coherent format");
        }
    }

}
