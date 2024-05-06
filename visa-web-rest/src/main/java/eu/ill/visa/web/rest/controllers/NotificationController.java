package eu.ill.visa.web.rest.controllers;

import com.github.dozermapper.core.Mapper;
import eu.ill.visa.business.services.ClientNotificationService;
import eu.ill.visa.core.domain.ClientNotification;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.security.tokens.AccountToken;
import eu.ill.visa.web.rest.dtos.NotificationPayloadDto;
import eu.ill.visa.web.rest.dtos.SystemNotificationDto;
import eu.ill.visa.web.rest.module.MetaResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


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
    public MetaResponse<NotificationPayloadDto> getNotifications(@Context final SecurityContext securityContext) {
        List<SystemNotificationDto> systemNotifications = this.clientNotificationService.getAllActiveSystemNotifications()
            .stream()
            .map(systemNotification -> mapper.map(systemNotification, SystemNotificationDto.class))
            .toList();

        NotificationPayloadDto notificationPayload = new NotificationPayloadDto();
        notificationPayload.setSystemNotifications(systemNotifications);

        // Check for auth and admin user
        if (securityContext.isSecure()) {
            AccountToken accountToken = (AccountToken)securityContext.getUserPrincipal();
            User user = accountToken.getUser();
            if (user.hasRole(Role.ADMIN_ROLE)) {

                List<ClientNotification> clientNotifications = this.clientNotificationService.getAllAdminNotifications();
                notificationPayload.setAdminNotifications(clientNotifications);
            }
        }

        return createResponse(notificationPayload);
    }

}
