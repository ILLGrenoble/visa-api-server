package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import jakarta.persistence.*;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceActivity.cleanup", query = """
            DELETE FROM InstanceActivity a
            WHERE a.createdAt < :date
    """),
})
@Table(name = "instance_activity")
public class InstanceActivity extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @JoinColumn(name = "user_id")
    private String userId;

    @Column(name = "instance_id", nullable = false)
    @JoinColumn(name = "instance_id")
    private Long instanceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 50, nullable = false)
    private InstanceActivityType instanceActivityType;

    public InstanceActivity() {
    }

    public InstanceActivity(String userId, Long instanceId, InstanceActivityType type) {
        this.userId = userId;
        this.instanceId = instanceId;
        this.instanceActivityType = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public InstanceActivityType getInstanceActivityType() {
        return instanceActivityType;
    }

    public void setInstanceActivityType(InstanceActivityType instanceActivityType) {
        this.instanceActivityType = instanceActivityType;
    }

    /**
     * Entity mapping used to force foreign key constraints onto instance_id and user_id. This entity is not used elsewhere.
     */
    @Entity
    @Table(name = "instance_activity")
    private static class InstanceActivityInner {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = true)
        private User user;

        @ManyToOne
        @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
        private Instance instance;
    }
}
