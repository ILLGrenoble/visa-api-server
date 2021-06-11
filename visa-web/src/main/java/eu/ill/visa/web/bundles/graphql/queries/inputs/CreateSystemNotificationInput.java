package eu.ill.visa.web.bundles.graphql.queries.inputs;

import eu.ill.visa.core.domain.enumerations.SystemNotificationLevel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateSystemNotificationInput {

    @NotNull
    @Size(max = 250)
    private String message;

    @NotNull
    private SystemNotificationLevel level;


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


}
