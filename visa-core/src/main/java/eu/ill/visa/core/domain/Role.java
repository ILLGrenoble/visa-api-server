package eu.ill.visa.core.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class Role implements Serializable {

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String STAFF_ROLE = "STAFF";
    public static final String INSTRUMENT_CONTROL_ROLE   = "INSTRUMENT_CONTROL";
    public static final String INSTRUMENT_SCIENTIST_ROLE = "INSTRUMENT_SCIENTIST";
    public static final String IT_SUPPORT_ROLE           = "IT_SUPPORT";
    public static final String SCIENTIFIC_COMPUTING_ROLE = "SCIENTIFIC_COMPUTING";

    private Long id;

    private String name;

    private String description;


    Role() {

    }

    public Role(String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Role) {
            final Role other = (Role) object;
            return new EqualsBuilder()
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


    public boolean isName(String name) {
        return this.name.equalsIgnoreCase(name);
    }
}
