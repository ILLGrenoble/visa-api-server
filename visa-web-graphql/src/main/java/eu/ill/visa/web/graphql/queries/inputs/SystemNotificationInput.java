package eu.ill.visa.web.graphql.queries.inputs;

import eu.ill.visa.core.entity.enumerations.SystemNotificationLevel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class SystemNotificationInput {

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:s");

    @NotNull
    @Size(max = 250)
    private String message;

    @NotNull
    private SystemNotificationLevel level;

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

    public String getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(String activatedAt) {
        this.activatedAt = activatedAt;
    }
}
