package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.enumerations.BookingRequestState;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @NamedQuery(name = "bookingRequest.getByUid", query = """
        SELECT br
        FROM BookingRequest br
        WHERE br.uid = :uid
        AND br.deletedAt IS NULL
    """),
    @NamedQuery(name = "bookingRequest.getAll", query = """
        SELECT br
        FROM BookingRequest br
        WHERE br.deletedAt IS NULL
        AND br.endDate >= CURRENT_DATE()
        AND br.state IN ('CREATED', 'ACCEPTED')
        ORDER BY br.id
    """),
    @NamedQuery(name = "bookingRequest.getAllForOwner", query = """
        SELECT br
        FROM BookingRequest br
        WHERE br.deletedAt IS NULL
        AND br.endDate >= CURRENT_DATE()
        AND br.state IN ('CREATED', 'ACCEPTED')
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

    @Column(name = "uid", length = 16, nullable = false)
    private String uid;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

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

    public static Builder Builder() {
        return new Builder();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    @Transient
    public LocalDateTime getDayAfterEndDate() {
        return this.endDate.plusDays(1);
    }

    public void setEndDate(LocalDateTime endDate) {
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

    @Transient
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();

        return this.state.equals(BookingRequestState.ACCEPTED) && !now.isBefore(this.startDate) && !now.isAfter(this.endDate);
    }

    @Transient
    public String getCreationComments() {
        return this.history.stream()
            .filter(element -> element.getState().equals(BookingRequestState.CREATED))
            .findFirst()
            .map(BookingRequestHistory::getComments)
            .orElse(null);
    }

    @Transient
    public String getLatestCreationComments() {
        return this.history.stream()
            .filter(element -> element.getState().equals(BookingRequestState.CREATED))
            .sorted((h1, h2) -> h1.getId() == null ? 1 : h2.getId() == null ? -1 : h1.getId().compareTo(h2.getId()))
            .map(BookingRequestHistory::getComments)
            .toList()
            .getLast();
    }

    @Transient
    public String getValidationComments() {
        return this.history.stream()
            .filter(element -> element.getState().equals(BookingRequestState.ACCEPTED) || element.getState().equals(BookingRequestState.REFUSED))
            .findFirst()
            .map(BookingRequestHistory::getComments)
            .orElse(null);
    }

    public String toString() {
        final LocalDate startDate = this.startDate.toLocalDate();
        final LocalDate endDate = this.endDate.toLocalDate();

        String formatted = format("BookingRequest id: %d, owner: %s, start: %s, end %s",  id, owner.getFullNameAndId(), startDate, endDate);
        String formattedFlavours = this.flavours.stream().map(BookingRequestFlavour::toString).collect(Collectors.joining(", "));
        return format("%s. Flavours: %s", formatted, formattedFlavours);
    }

    public static final class Builder {
        private String uid;
        private String name;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private User owner;
        private String comments;
        private List<BookingRequestFlavour> flavours;

        public Builder uid(String uid) {
            this.uid = uid;
            return this;
        }
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public Builder comments(String comments) {
            this.comments = comments;
            return this;
        }

        public Builder flavours(List<BookingRequestFlavour> flavours) {
            this.flavours = flavours;
            return this;
        }

        public BookingRequest build() {
            BookingRequest request = new BookingRequest();
            request.uid = uid;
            request.name = name;
            request.startDate = startDate;
            request.endDate = endDate;
            request.owner = owner;
            request.state = BookingRequestState.CREATED;
            request.history.add(new BookingRequestHistory(BookingRequestState.CREATED, comments, owner));
            request.flavours = flavours;
            return request;
        }
    }



}
