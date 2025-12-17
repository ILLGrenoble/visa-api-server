package eu.ill.visa.core.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@NamedQueries({
    @NamedQuery(name = "bookingToken.getById", query = """
        SELECT bt
        FROM BookingToken bt
        WHERE bt.id = :id
    """),
    @NamedQuery(name = "bookingToken.getByUid", query = """
        SELECT bt
        FROM BookingToken bt
        WHERE bt.uid = :uid
    """),
    @NamedQuery(name = "bookingToken.getAllForBookingRequestId", query = """
        SELECT bt
        FROM BookingToken bt
        LEFT JOIN BookingRequest br ON bt.bookingRequest = br
        WHERE br.id = :bookingRequestId
        ORDER By bt.id
    """),
    @NamedQuery(name = "bookingToken.getAllActiveUnassigned", query = """
        SELECT bt
        FROM BookingToken bt
        LEFT JOIN BookingRequest br ON bt.bookingRequest = br
        WHERE bt.deletedAt IS NULL
        AND bt.instance IS NULL
        AND br.endDate >= CURRENT_DATE()
        AND br.startDate <= CURRENT_DATE()
        AND br.deletedAt IS NULL
        AND br.state IN ('CREATED', 'ACCEPTED')
        ORDER BY bt.id
    """),
})
@Table(name = "booking_token")
public class BookingToken extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uid", length = 16, nullable = false)
    private String uid;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_request_id", foreignKey = @ForeignKey(name = "fk_booking_request_id"), nullable = false)
    private BookingRequest bookingRequest;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "flavour_id", foreignKey = @ForeignKey(name = "fk_flavour_id"), nullable = false)
    private Flavour flavour;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = true)
    private Instance instance;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = true)
    private User owner;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_token_id", foreignKey = @ForeignKey(name = "fk_booking_token_id"), nullable = false)
    private List<BookingTokenHistory> history = new ArrayList<>();

    @Column(name = "deleted_at", nullable = true)
    private Date deletedAt;

    public BookingToken() {
    }

    public BookingToken(final BookingRequest bookingRequest, final Flavour flavour, final String uid) {
        this.bookingRequest = bookingRequest;
        this.flavour = flavour;
        this.uid = uid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public BookingRequest getBookingRequest() {
        return bookingRequest;
    }

    public void setBookingRequest(BookingRequest bookingRequest) {
        this.bookingRequest = bookingRequest;
    }

    public Flavour getFlavour() {
        return flavour;
    }

    public void setFlavour(Flavour flavour) {
        this.flavour = flavour;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<BookingTokenHistory> getHistory() {
        return history;
    }

    public void setHistory(List<BookingTokenHistory> history) {
        this.history = history;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }
}
