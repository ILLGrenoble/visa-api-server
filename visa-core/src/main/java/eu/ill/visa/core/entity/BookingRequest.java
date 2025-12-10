package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.BookingRequestState;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Entity
@NamedQueries({
    @NamedQuery(name = "bookingRequest.getById", query = """
        SELECT br
        FROM BookingRequest br
        WHERE br.id = :id
        AND br.deletedAt IS NULL
    """),
    @NamedQuery(name = "bookingRequest.getAll", query = """
        SELECT br
        FROM BookingRequest br
        WHERE br.deletedAt IS NULL
        AND br.endDate >= CURRENT_DATE()
        ORDER BY br.id
    """),
    @NamedQuery(name = "bookingRequest.getAllForOwner", query = """
        SELECT br
        FROM BookingRequest br
        WHERE br.deletedAt IS NULL
        AND br.endDate >= CURRENT_DATE()
        AND br.owner.id = :ownerId
        ORDER BY br.id
    """),
    @NamedQuery(name = "bookingRequest.getAllHistoricForOwner", query = """
        SELECT br
        FROM BookingRequest br
        WHERE br.endDate < CURRENT_DATE()
        AND br.owner.id = :ownerId
        ORDER BY br.id
    """),
    @NamedQuery(name = "bookingRequest.getAllPending", query = """
        SELECT br
        FROM BookingRequest br
        WHERE br.deletedAt IS NULL
        AND br.endDate >= CURRENT_DATE()
        AND br.state = 'CREATED'
        ORDER BY br.id
    """),
})
@Table(name = "booking_request")
public class BookingRequest extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 50, nullable = false)
    private BookingRequestState state;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_request_id", foreignKey = @ForeignKey(name = "fk_booking_request_id"), nullable = false)
    private List<BookingRequestFlavour> flavours = new ArrayList<>();

    @Column(name = "deleted_at", nullable = true)
    private Date deletedAt;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_request_id", foreignKey = @ForeignKey(name = "fk_booking_request_id"), nullable = false)
    private List<BookingRequestHistory> history = new ArrayList<>();

    public static BookingRequest Create(Date startDate, Date endDate, User owner, String comments, List<BookingRequestFlavour> flavours) {
        return new BookingRequest(startDate, endDate, owner, comments, flavours);
    }

    public BookingRequest() {
    }

    private BookingRequest(Date startDate, Date endDate, User owner, String comments, List<BookingRequestFlavour> flavours) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.owner = owner;
        this.state = BookingRequestState.CREATED;
        this.flavours = flavours;
        this.history.add(new BookingRequestHistory(BookingRequestState.CREATED, comments, owner));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public BookingRequestState getState() {
        return state;
    }

    public void setState(BookingRequestState state) {
        this.state = state;
    }

    public List<BookingRequestFlavour> getFlavours() {
        return flavours;
    }

    public void setFlavours(List<BookingRequestFlavour> flavours) {
        this.flavours = flavours;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<BookingRequestHistory> getHistory() {
        return history;
    }

    public void setHistory(List<BookingRequestHistory> history) {
        this.history = history;
    }

    public String toString() {
        final LocalDate startDate = this.startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate endDate = this.endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String formatted = format("BookingRequest id: %d, owner: %s, start: %s, end %s",  id, owner.getFullNameAndId(), startDate, endDate);
        String formattedFlavours = this.flavours.stream().map(BookingRequestFlavour::toString).collect(Collectors.joining(", "));
        return format("%s. Flavours: %s", formatted, formattedFlavours);
    }

}
