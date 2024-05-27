package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.SystemNotification;
import eu.ill.visa.core.entity.enumerations.SystemNotificationLevel;

import java.util.Date;

public class SystemNotificationDto {

    private final Long id;
    private final Long uid;
    private final String message;
    private final SystemNotificationLevel level;
    private final Date createdAt;
    private final Date activatedAt;

    public SystemNotificationDto(final SystemNotification systemNotification) {
        this.id = systemNotification.getId();
        this.uid = systemNotification.getUid();
        this.message = systemNotification.getMessage();
        this.level = systemNotification.getLevel();
        this.createdAt = systemNotification.getCreatedAt();
        this.activatedAt = systemNotification.getActivatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getUid() {
        return uid;
    }

    public String getMessage() {
        return message;
    }

    public SystemNotificationLevel getLevel() {
        return level;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getActivatedAt() {
        return activatedAt;
    }
}
