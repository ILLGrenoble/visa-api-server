package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import jakarta.persistence.*;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceActivity.getAll", query = """
            SELECT a FROM InstanceActivity a
            ORDER BY a.id
    """),
    @NamedQuery(name = "instanceActivity.getAllForUser", query = """
            SELECT a FROM InstanceActivity a
            WHERE a.user = :user
            ORDER BY a.id
    """),
    @NamedQuery(name = "instanceActivity.getAllForInstance", query = """
            SELECT a FROM InstanceActivity a
            WHERE a.instance = :instance
            ORDER BY a.id
    """),
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

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private Instance instance;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 50, nullable = false)
    private InstanceActivityType instanceActivityType;

    public InstanceActivity() {

    }

    public InstanceActivity(InstanceActivityType type) {
        this.instanceActivityType = type;
    }

    public InstanceActivity(User user, Instance instance, InstanceActivityType type) {
        this.user = user;
        this.instance = instance;
        this.instanceActivityType = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public InstanceActivityType getInstanceActivityType() {
        return instanceActivityType;
    }

    public void setInstanceActivityType(InstanceActivityType instanceActivityType) {
        this.instanceActivityType = instanceActivityType;
    }
}
