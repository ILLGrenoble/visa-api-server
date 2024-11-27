package eu.ill.visa.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.Set;


@Entity
@NamedQueries({
    @NamedQuery(name = "experiment.getById", query = """
            SELECT e
            FROM Experiment e
            WHERE e.id = :id
            AND e.startDate IS NOT NULL
            AND e.endDate IS NOT NULL
    """),
    @NamedQuery(name = "experiment.getYearsForUser", query = """
            SELECT distinct YEAR(e.startDate)
            FROM Experiment e
            JOIN e.users u
            WHERE u = :user
            AND e.startDate IS NOT NULL
            AND e.endDate IS NOT NULL
    """),
    @NamedQuery(name = "experiment.getYearsForOpenData", query = """
            SELECT distinct YEAR(e.startDate)
            FROM Experiment e
            JOIN e.proposal p
            WHERE p.publicAt <= :currentDate
    """),
    @NamedQuery(name = "experiment.getByIdAndUser", query = """
            SELECT e
            FROM Experiment e
            JOIN e.users u
            WHERE u = :user
            AND e.id = :id
            AND e.startDate IS NOT NULL
            AND e.endDate IS NOT NULL
    """),
    @NamedQuery(name = "experiment.getByIdForOpenData", query = """
            SELECT e
            FROM Experiment e
            JOIN e.proposal p
            WHERE p.publicAt <= :currentDate
            AND e.id = :id
            AND e.startDate IS NOT NULL
            AND e.endDate IS NOT NULL
    """),
    @NamedQuery(name = "experiment.getAllForInstanceId", query = """
            SELECT e
            FROM Experiment e
            JOIN e.instances i
            WHERE i.id = :instanceId
            AND i.deletedAt IS NULL
            AND e.startDate IS NOT NULL
            AND e.endDate IS NOT NULL
    """),
})
@Table(name = "experiment")
public class Experiment {

    @Id
    @Column(name = "id", length = 32, nullable = false)
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
    @ManyToMany()
    @JoinTable(
        name = "experiment_user",
        joinColumns = @JoinColumn(name = "experiment_id", foreignKey = @ForeignKey(name = "fk_experiment_id")),
        inverseJoinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"))
    )
    private Set<User> users;

    @JsonIgnore
    @ManyToMany()
    @JoinTable(
        name = "instance_experiment",
        joinColumns = @JoinColumn(name = "experiment_id", foreignKey = @ForeignKey(name = "fk_experiment_id")),
        inverseJoinColumns = @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"))
    )
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
