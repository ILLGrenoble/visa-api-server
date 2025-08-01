package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.SystemNotificationLevel;
import jakarta.persistence.*;

import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@Entity
@NamedQueries({
    @NamedQuery(name = "systemNotification.getById", query = """
            SELECT s FROM SystemNotification s WHERE s.id = :id AND s.deletedAt IS NULL
    """),
    @NamedQuery(name = "systemNotification.getAll", query = """
            SELECT s FROM SystemNotification s WHERE s.deletedAt IS NULL ORDER BY s.id
    """),
    @NamedQuery(name = "systemNotification.getAllActive", query = """
            SELECT s FROM SystemNotification s WHERE s.deletedAt IS NULL AND s.activatedAt IS NOT NULL ORDER BY s.id DESC
    """),
})
@Table(name = "system_notification")
public class SystemNotification extends Timestampable {

    public enum SystemNotificationType {
        BANNER,
        MODAL,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "message", length = 4096, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 50, nullable = false)
    private SystemNotificationLevel level;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = true)
    private SystemNotificationType type;

    @Column(name = "activated_at", nullable = true)
    private Date activatedAt;

    @Column(name = "deleted_at", nullable = true)
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

    public SystemNotificationType getType() {
        return type;
    }

    public void setType(SystemNotificationType type) {
        this.type = type;
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
