package eu.ill.visa.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class Experiment implements Serializable {

    private String id;
    private Instrument instrument;
    private Proposal proposal;
    private Date startDate;
    private Date endDate;

    @JsonIgnore
    private Set<User> users;

    @JsonIgnore
    private Set<Instance> instances;

    private Experiment(Builder builder) {
        this.id = builder.id;
        this.instrument = builder.instrument;
        this.proposal = builder.proposal;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
    }

    public Experiment() {

    }

    public static Builder newExperiment() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public Proposal getProposal() {
        return proposal;
    }

    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Instance> getInstances() {
        return instances;
    }

    public void setInstances(Set<Instance> instances) {
        this.instances = instances;
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

    @Override
    public boolean equals(Object object) {
        if (object instanceof Experiment) {
            final Experiment other = (Experiment) object;
            return new EqualsBuilder()
                .append(id, other.id)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("instrument", instrument)
            .append("proposal", proposal)
            .append("startDate", startDate)
            .append("endDate", endDate)
            .toString();
    }


    public static final class Builder {
        private String id;
        private Instrument instrument;
        private Proposal proposal;
        private Date startDate;
        private Date endDate;

        private Builder() {
        }

        public Experiment build() {
            return new Experiment(this);
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder instrument(Instrument instrument) {
            this.instrument = instrument;
            return this;
        }

        public Builder proposal(Proposal proposal) {
            this.proposal = proposal;
            return this;
        }

        public Builder startDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(Date endDate) {
            this.endDate = endDate;
            return this;
        }
    }
}
