package eu.ill.visa.core.domain;

import eu.ill.visa.core.domain.enumerations.SystemNotificationLevel;

public class SystemNotification extends Timestampable {
    private Long id;
    private String message;
    private SystemNotificationLevel level;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
