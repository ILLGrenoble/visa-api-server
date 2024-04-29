package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

@Entity
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
