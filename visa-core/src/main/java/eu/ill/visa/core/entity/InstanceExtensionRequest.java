package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.InstanceExtensionRequestState;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceExtensionRequest.getAll", query = """
            SELECT r
            FROM InstanceExtensionRequest r
            WHERE r.state = 'PENDING'
            AND r.instance.state != 'DELETED'
            ORDER BY r.id
    """),
    @NamedQuery(name = "instanceExtensionRequest.getById", query = """
            SELECT r
            FROM InstanceExtensionRequest r
            WHERE r.state = 'PENDING'
            AND r.instance.state != 'DELETED'
            AND r.id = :id
    """),
    @NamedQuery(name = "instanceExtensionRequest.getForInstance", query = """
            SELECT r
            FROM InstanceExtensionRequest r
            WHERE r.state = 'PENDING'
            AND r.instance = :instance
    """),
})
@Table(name = "instance_extension_request")
public class InstanceExtensionRequest extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private Instance instance;

    @Column(name = "comments", length = 4000, nullable = true)
    private String comments;

    @ManyToOne(optional = true)
    @JoinColumn(name = "handler_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = true)
    private User handler;

    @Column(name = "handled_on", nullable = true)
    private Date handledOn;

    @Column(name = "handler_comments", length = 4000, nullable = true)
    private String handlerComments;

    @Column(name = "original_termination_date", nullable = false)
    private Date originalTerminationDate;

    @Column(name = "extension_date", nullable = true)
    private Date extensionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 50, nullable = false)
    private InstanceExtensionRequestState state;

    public InstanceExtensionRequest() {
    }

    public InstanceExtensionRequest(Instance instance, String comments) {
        this.instance = instance;
        this.comments = comments;
        this.state = InstanceExtensionRequestState.PENDING;
        this.originalTerminationDate = instance.getTerminationDate();
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

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public User getHandler() {
        return handler;
    }

    public void setHandler(User handler) {
        this.handler = handler;
    }

    public Date getHandledOn() {
        return handledOn;
    }

    public void setHandledOn(Date handledOn) {
        this.handledOn = handledOn;
    }

    public String getHandlerComments() {
        return handlerComments;
    }

    public void setHandlerComments(String handlerComments) {
        this.handlerComments = handlerComments;
    }

    public InstanceExtensionRequestState getState() {
        return state;
    }

    public void setState(InstanceExtensionRequestState state) {
        this.state = state;
    }

    public Date getOriginalTerminationDate() {
        return originalTerminationDate;
    }

    public void setOriginalTerminationDate(Date originalTerminationDate) {
        this.originalTerminationDate = originalTerminationDate;
    }

    public Date getExtensionDate() {
        return extensionDate;
    }

    public void setExtensionDate(Date extensionDate) {
        this.extensionDate = extensionDate;
    }
}
