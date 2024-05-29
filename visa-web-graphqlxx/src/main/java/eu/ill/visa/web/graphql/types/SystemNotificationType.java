package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.SystemNotification;
import eu.ill.visa.core.entity.enumerations.SystemNotificationLevel;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

import java.util.Date;

public class SystemNotificationType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String message;
    private final SystemNotificationLevel level;
    private final Date activatedAt;
    private final Date deletedAt;

    public SystemNotificationType(final SystemNotification systemNotification) {
        this.id = systemNotification.getId();
        this.message = systemNotification.getMessage();
        this.level = systemNotification.getLevel();
        this.activatedAt = systemNotification.getActivatedAt();
        this.deletedAt = systemNotification.getDeletedAt();
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public SystemNotificationLevel getLevel() {
        return level;
    }

    public Date getActivatedAt() {
        return activatedAt;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }
}
