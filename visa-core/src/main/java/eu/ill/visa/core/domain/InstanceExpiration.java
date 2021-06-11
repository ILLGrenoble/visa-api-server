package eu.ill.visa.core.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

public class InstanceExpiration extends Timestampable {

    private Long id;
    private Instance instance;
    private Date expirationDate;

    public InstanceExpiration() {
    }

    public InstanceExpiration(Instance instance, Date expirationDate) {
        this.instance = instance;
        this.expirationDate = expirationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InstanceExpiration that = (InstanceExpiration) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(instance, that.instance)
            .append(expirationDate, that.expirationDate)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(instance)
            .append(expirationDate)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("instance", instance)
            .append("expirationDate", expirationDate)
            .toString();
    }
}
