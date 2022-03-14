package eu.ill.visa.core.domain;

import eu.ill.visa.core.domain.enumerations.SystemNotificationLevel;

import javax.persistence.Transient;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class SystemNotification extends Timestampable {

    private Long id;
    private String message;
    private SystemNotificationLevel level;
    private Date activatedAt;
    private Date deletedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Transient
    public Long getUid() {
        final StringBuilder sb = new StringBuilder();
        sb.append(id).append("-").append(message).append("-").append(level).append("-").append(activatedAt.getTime());
        String data = sb.toString();
        Checksum crc32 = new CRC32();
        crc32.update(data.getBytes(), 0, data.length());
        return crc32.getValue();
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

    public Date getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Date activatedAt) {
        this.activatedAt = activatedAt;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }
}
