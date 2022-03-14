package eu.ill.visa.web.bundles.graphql.queries.inputs;

import eu.ill.visa.core.domain.enumerations.SystemNotificationLevel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
