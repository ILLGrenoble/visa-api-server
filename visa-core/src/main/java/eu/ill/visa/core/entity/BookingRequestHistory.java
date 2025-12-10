package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.BookingRequestState;
import jakarta.persistence.*;

@Entity
@NamedQueries({

})
@Table(name = "booking_request_history")
public class BookingRequestHistory extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 50, nullable = false)
    private BookingRequestState state;

    @Column(name = "comments", length = 2500, nullable = true)
    private String comments;

    @ManyToOne(optional = false)
    @JoinColumn(name = "actor_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = false)
    private User actor;

    public BookingRequestHistory() {
    }

    public BookingRequestHistory(BookingRequestState state, String comments, User actor) {
        this.state = state;
        this.comments = comments;
        this.actor = actor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookingRequestState getState() {
        return state;
    }

    public void setState(BookingRequestState state) {
        this.state = state;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }
}
