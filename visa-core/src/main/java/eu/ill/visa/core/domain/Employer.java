package eu.ill.visa.core.domain;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "employer")
public class Employer {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 200, nullable = true)
    private String name;

    @Column(name = "town", length = 100, nullable = true)
    private String town;

    @Column(name = "country_code", length = 10, nullable = true)
    private String countryCode;

    public Employer() {
    }

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

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Employer employer = (Employer) o;

        return new EqualsBuilder()
            .append(id, employer.id)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("town", town)
            .append("countryCode", countryCode)
            .toString();
    }
}
