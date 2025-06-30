package eu.ill.visa.web.graphql.inputs;

import eu.ill.visa.core.entity.SystemNotification;
import eu.ill.visa.core.entity.enumerations.SystemNotificationLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.graphql.Input;

@Input("SystemNotificationInput")
public class SystemNotificationInput {

    @Size(max = 4096)
    private @NotNull String message;
    private @NotNull SystemNotificationLevel level;
    private @NotNull SystemNotification.SystemNotificationType type;
    private String activatedAt;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SystemNotificationLevel getLevel() {
        return level;
    }

    public void setLevel(SystemNotificationLevel level) {
        this.level = level;
    }

    public SystemNotification.SystemNotificationType getType() {
        return type;
    }

    public void setType(SystemNotification.SystemNotificationType type) {
        this.type = type;
    }

    public String getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(String activatedAt) {
        this.activatedAt = activatedAt;
    }
}
