package eu.ill.visa.core.entity;

import jakarta.persistence.*;

import static java.lang.String.format;

@Entity
@NamedQueries({

})
@Table(name = "booking_request_flavour")
public class BookingRequestFlavour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "flavour_id", foreignKey = @ForeignKey(name = "fk_flavour_id"), nullable = false)
    private Flavour flavour;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    public BookingRequestFlavour() {
    }

    public BookingRequestFlavour(Flavour flavour, Long quantity) {
        this.flavour = flavour;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Flavour getFlavour() {
        return flavour;
    }

    public void setFlavour(Flavour flavour) {
        this.flavour = flavour;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String toString() {
        return format("%s, quantity: %d", flavour.getName(), quantity);
    }
}
