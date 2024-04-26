package eu.ill.visa.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.Set;


@Entity
@Table(name = "experiment")
public class Experiment {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "instrument_id", foreignKey = @ForeignKey(name = "fk_instrument_id"), nullable = false)
    private Instrument instrument;

    @ManyToOne
    @JoinColumn(name = "proposal_id", foreignKey = @ForeignKey(name = "fk_proposal_id"), nullable = false)
    private Proposal proposal;

    @Column(name = "title", length = 2000, nullable = true)
    private String title;

    @Column(name = "url", length = 2000, nullable = true)
    private String url;

    @Column(name = "doi", length = 2000, nullable = true)
    private String doi;

    @Column(name = "start_date", nullable = true)
    private Date startDate;

    @Column(name = "end_date", nullable = true)
    private Date endDate;

    @JsonIgnore
    private Set<User> users;

    @JsonIgnore
    private Set<Instance> instances;

    public Experiment() {

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
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
            .append("title", title)
            .append("url", url)
            .append("doi", doi)
            .append("startDate", startDate)
            .append("endDate", endDate)
            .toString();
    }
}
