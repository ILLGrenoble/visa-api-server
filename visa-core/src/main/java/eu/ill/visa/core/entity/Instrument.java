package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

@Entity
@NamedQueries({
    @NamedQuery(name = "instrument.getById", query = """
            SELECT i FROM Instrument i WHERE i.id = :id
    """),
    @NamedQuery(name = "instrument.getAllForUser", query = """
            SELECT i FROM Instrument i
            WHERE i IN (
                SELECT DISTINCT e.instrument
                FROM Experiment e
                JOIN e.users u
                WHERE u = :user
                AND e.startDate IS NOT NULL
                AND e.endDate IS NOT NULL
            )
            ORDER BY i.name ASC
    """),
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "instrument.getAll", resultClass = Instrument.class, query = """
            SELECT i.id, i.name
            FROM instrument i
            WHERE i.id IN (SELECT DISTINCT e.instrument_id FROM experiment e)
            UNION
            SELECT i.id, i.name
            FROM instrument i, instrument_scientist ins
            WHERE ins.instrument_id = i.id
            ORDER by name
    """),
    @NamedNativeQuery(name = "instrument.getAllForExperimentsAndInstrumentScientist", resultClass = Instrument.class, query = """
            SELECT DISTINCT i.id, i.name
            FROM instrument i, experiment e
            WHERE e.instrument_id = i.id
            AND e.id in :experimentIds
            UNION
            select DISTINCT i.id, i.name
            FROM instrument i, instrument_scientist ir
            WHERE ir.instrument_id = i.id
            AND ir.user_id = :userId
    """),
    @NamedNativeQuery(name = "instrument.getAllForInstrumentScientist", resultClass = Instrument.class, query = """
            select DISTINCT i.id, i.name
            FROM instrument i, instrument_scientist ir
            WHERE ir.instrument_id = i.id
            AND ir.user_id = :userId
    """),
})
@Table(name = "instrument")
public class Instrument {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @ManyToMany()
    @JoinTable(
        name = "instrument_scientist",
        joinColumns = @JoinColumn(name = "instrument_id", foreignKey = @ForeignKey(name = "fk_instrument_id")),
        inverseJoinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"))
    )
    private List<User> scientists;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getScientists() {
        return scientists;
    }

    public void setScientists(List<User> scientists) {
        this.scientists = scientists;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Instrument) {
            final Instrument other = (Instrument) object;
            return new EqualsBuilder()
                .append(id, other.id)
                .append(name, other.name)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(name)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .toString();
    }


}
