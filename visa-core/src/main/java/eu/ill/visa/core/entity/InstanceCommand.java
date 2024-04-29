package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.InstanceCommandState;
import eu.ill.visa.core.entity.enumerations.InstanceCommandType;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceCommand.getById", query = """
            SELECT a FROM InstanceCommand a
            WHERE a.id = :id
    """),
    @NamedQuery(name = "instanceCommand.getAll", query = """
            SELECT a FROM InstanceCommand a
            ORDER BY a.id
    """),
    @NamedQuery(name = "instanceCommand.getAllActive", query = """
            SELECT a FROM InstanceCommand a
            WHERE a.state IN ('PENDING', 'QUEUED', 'RUNNING')
            ORDER BY a.id
    """),
    @NamedQuery(name = "instanceCommand.getAllPending", query = """
            SELECT a FROM InstanceCommand a
            WHERE a.state = 'PENDING'
            ORDER BY a.id
    """),
    @NamedQuery(name = "instanceCommand.getAllForUser", query = """
            SELECT a FROM InstanceCommand a
            WHERE a.user = :user
            ORDER BY a.id
    """),
    @NamedQuery(name = "instanceCommand.getAllForInstance", query = """
            SELECT a FROM InstanceCommand a
            WHERE a.instance = :instance
            ORDER BY a.id
    """),
})
@Table(name = "instance_command")
public class InstanceCommand extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = true)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private Instance instance;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 50, nullable = false)
    private InstanceCommandType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 50, nullable = false)
    private InstanceCommandState state;

    @Column(name = "message", length = 255, nullable = true)
    private String message;

    public InstanceCommand() {
    }

    public InstanceCommand(User user, Instance instance, InstanceCommandType actionType) {
        this.user = user;
        this.instance = instance;
        this.actionType = actionType;
        this.state = InstanceCommandState.PENDING;
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

    public InstanceCommandType getActionType() {
        return actionType;
    }

    public void setActionType(InstanceCommandType actionType) {
        this.actionType = actionType;
    }

    public InstanceCommandState getState() {
        return state;
    }

    public void setState(InstanceCommandState state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InstanceCommand that = (InstanceCommand) o;

        return new EqualsBuilder()
            .append(user, that.user)
            .append(instance, that.instance)
            .append(actionType, that.actionType)
            .append(state, that.state)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(user)
            .append(instance)
            .append(actionType)
            .append(state)
            .toHashCode();
    }
}
