package eu.ill.visa.web.controllers;

import com.google.inject.Inject;
import eu.ill.visa.business.services.SystemNotificationService;
import eu.ill.visa.web.dtos.SystemNotificationDto;
import org.dozer.Mapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/notifications")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class NotificationController extends AbstractController {

    private final Mapper mapper;
    private final SystemNotificationService systemNotificationService;

    @Inject
    NotificationController(final Mapper mapper, final SystemNotificationService systemNotificationService) {
        this.mapper = mapper;
        this.systemNotificationService = systemNotificationService;
    }

    @GET
    public Response getNotifications() {
        List<SystemNotificationDto> notifications = this.systemNotificationService.getAllActive()
            .stream()
            .map(systemNotification -> mapper.map(systemNotification, SystemNotificationDto.class))
            .collect(Collectors.toUnmodifiableList());

        return createResponse(notifications);
    }

}
