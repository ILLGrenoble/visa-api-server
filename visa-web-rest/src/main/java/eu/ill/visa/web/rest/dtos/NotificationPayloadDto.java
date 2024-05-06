package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.domain.ClientNotification;

import java.util.List;

public class NotificationPayloadDto {

    private List<SystemNotificationDto> systemNotifications;
    private List<ClientNotification> clientNotifications;

    public List<SystemNotificationDto> getSystemNotifications() {
        return systemNotifications;
    }

    public void setSystemNotifications(List<SystemNotificationDto> systemNotifications) {
        this.systemNotifications = systemNotifications;
    }

    public List<ClientNotification> getAdminNotifications() {
        return clientNotifications;
    }

    public void setAdminNotifications(List<ClientNotification> clientNotifications) {
        this.clientNotifications = clientNotifications;
    }
}
