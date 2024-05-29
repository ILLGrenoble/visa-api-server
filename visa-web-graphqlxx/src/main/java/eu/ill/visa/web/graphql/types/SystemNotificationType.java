package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.SystemNotification;
import eu.ill.visa.core.entity.enumerations.SystemNotificationLevel;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("SystemNotification")
public class SystemNotificationType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String message;
    private final @NotNull SystemNotificationLevel level;
    private final Date activatedAt;

    public SystemNotificationType(final SystemNotification systemNotification) {
        this.id = systemNotification.getId();
        this.message = systemNotification.getMessage();
        this.level = systemNotification.getLevel();
        this.activatedAt = systemNotification.getActivatedAt();
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
}
