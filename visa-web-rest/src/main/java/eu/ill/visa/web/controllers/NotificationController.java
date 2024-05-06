package eu.ill.visa.web.controllers;

import jakarta.inject.Inject;
import eu.ill.visa.business.services.ClientNotificationService;
import eu.ill.visa.core.domain.ClientNotification;
import eu.ill.visa.core.domain.Role;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.dtos.NotificationPayloadDto;
import eu.ill.visa.web.dtos.SystemNotificationDto;
import io.dropwizard.auth.Auth;
import org.dozer.Mapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/notifications")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class NotificationController extends AbstractController {

    private final Mapper mapper;
    private final ClientNotificationService clientNotificationService;

    @Inject
    NotificationController(final Mapper mapper, final ClientNotificationService clientNotificationService) {
        this.mapper = mapper;
        this.clientNotificationService = clientNotificationService;
    }

    @GET
    public Response getNotifications(@Auth final Optional<AccountToken> accountTokenOptional) {
        List<SystemNotificationDto> systemNotifications = this.clientNotificationService.getAllActiveSystemNotifications()
            .stream()
            .map(systemNotification -> mapper.map(systemNotification, SystemNotificationDto.class))
            .collect(Collectors.toUnmodifiableList());

        NotificationPayloadDto notificationPayload = new NotificationPayloadDto();
        notificationPayload.setSystemNotifications(systemNotifications);

        // Check for auth and admin user
        if (accountTokenOptional.isPresent()) {
            AccountToken accountToken = accountTokenOptional.get();
            User user = accountToken.getUser();
            if (user.hasRole(Role.ADMIN_ROLE)) {

                List<ClientNotification> clientNotifications = this.clientNotificationService.getAllAdminNotifications();
                notificationPayload.setAdminNotifications(clientNotifications);
            }
        }

        return createResponse(notificationPayload);
    }

}
