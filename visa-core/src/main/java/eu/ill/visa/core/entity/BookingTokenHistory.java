package eu.ill.visa.core.entity;

import jakarta.persistence.*;

@Entity
@NamedQueries({

})
@Table(name = "booking_token_history")
public class BookingTokenHistory extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = false)
    private User owner;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private Instance instance;

    public BookingTokenHistory() {
    }

    public BookingTokenHistory(User owner, Instance instance) {
        this.owner = owner;
        this.instance = instance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}
