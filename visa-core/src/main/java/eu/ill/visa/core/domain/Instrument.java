package eu.ill.visa.core.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

public class Instrument implements Serializable {

    private Long   id;
    private String     name;
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
