package eu.ill.visa.core.entity;

import jakarta.persistence.*;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceJupyterSession.getAll", query = """
        SELECT i
        FROM InstanceJupyterSession i
        WHERE i.active = true
        ORDER BY i.id DESC
    """),
    @NamedQuery(name = "instanceJupyterSession.countAll", query = """
        SELECT count(i)
        FROM InstanceJupyterSession i
        WHERE i.active = true
    """),
    @NamedQuery(name = "instanceJupyterSession.countAllInstances", query = """
            SELECT count(distinct i.instance)
            FROM InstanceJupyterSession i
            WHERE i.active = true
    """),
    @NamedQuery(name = "instanceJupyterSession.getAllByInstance", query = """
            SELECT i
            FROM InstanceJupyterSession i
            WHERE i.instance = :instance
            AND i.active = true
            ORDER BY i.id DESC
    """),
    @NamedQuery(name = "instanceJupyterSession.getByInstanceKernelSession", query = """
            SELECT i
            FROM InstanceJupyterSession i
            WHERE i.instance = :instance
            AND i.kernelId = :kernelId
            AND i.sessionId = :sessionId
            AND i.active = true
            ORDER BY i.id DESC
    """),
})
@Table(name = "instance_jupyter_session")
public class InstanceJupyterSession extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private Instance instance;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"))
    private User user;

    @Column(name = "kernel_id", length = 150, nullable = false)
    private String kernelId;

    @Column(name = "session_id", length = 150, nullable = false)
    private String sessionId;

    @Column(name = "active", nullable = false)
    private boolean active = false;

    public InstanceJupyterSession() {
    }

    public InstanceJupyterSession(Instance instance, User user, String kernelId, String sessionId) {
        this.instance = instance;
        this.user = user;
        this.kernelId = kernelId;
        this.sessionId = sessionId;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getKernelId() {
        return kernelId;
    }

    public void setKernelId(String kernelId) {
        this.kernelId = kernelId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
