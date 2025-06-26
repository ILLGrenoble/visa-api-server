package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

@Entity
@NamedQueries({
    @NamedQuery(name = "acknowledgedSystemNotification.getAllByUserId", query = """
        SELECT a
        FROM AcknowledgedSystemNotification a
        WHERE a.id.userId = :userId
    """),
})
@Table(name = "acknowledged_system_notification")
public class AcknowledgedSystemNotification {

    @EmbeddedId
    private AcknowledgedSystemNotificationKey id;

    @ManyToOne(optional = false)
    @MapsId("systemNotificationId")
    @JoinColumn(name = "system_notification_id", foreignKey = @ForeignKey(name = "fk_system_notification_id"), nullable = false)
    private SystemNotification systemNotification;

    @ManyToOne(optional = false)
    @MapsId("userId")
    @JoinColumn(name = "acknowledged_by_user_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = false)
    private User acknowledgedByUser;

    public AcknowledgedSystemNotification() {
    }

    public AcknowledgedSystemNotification(final SystemNotification notification,
                                          final User user) {
        this.systemNotification = notification;
        this.acknowledgedByUser = user;
        this.id = new AcknowledgedSystemNotificationKey(systemNotification.getId(), user.getId());
    }

    public AcknowledgedSystemNotificationKey getId() {
        return id;
    }

    public void setId(AcknowledgedSystemNotificationKey id) {
        this.id = id;
    }

    public SystemNotification getSystemNotification() {
        return systemNotification;
    }

    public void setSystemNotification(SystemNotification systemNotification) {
        this.systemNotification = systemNotification;
    }

    public User getAcknowledgedByUser() {
        return acknowledgedByUser;
    }

    public void setAcknowledgedByUser(User user) {
        this.acknowledgedByUser = user;
    }

    @Embeddable
    public static class AcknowledgedSystemNotificationKey implements Serializable {
        private Long systemNotificationId;
        private String userId;

        public AcknowledgedSystemNotificationKey() {
        }

        public AcknowledgedSystemNotificationKey(Long systemNotificationId, String userId) {
            this.systemNotificationId = systemNotificationId;
            this.userId = userId;
        }

        public Long getSystemNotificationId() {
            return systemNotificationId;
        }

        public void setSystemNotificationId(Long systemNotificationId) {
            this.systemNotificationId = systemNotificationId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            AcknowledgedSystemNotificationKey that = (AcknowledgedSystemNotificationKey) o;

            return new EqualsBuilder()
                .append(systemNotificationId, that.systemNotificationId)
                .append(userId, that.userId)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(systemNotificationId)
                .append(userId)
                .toHashCode();
        }
    }
}
