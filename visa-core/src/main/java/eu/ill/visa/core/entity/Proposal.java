package eu.ill.visa.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

@Entity
@Table(name = "proposal")
public class Proposal {

    @Id
    @Column(name = "id", nullable = false)
    private Long   id;

    @Column(name = "identifier", length = 100, nullable = false)
    private String identifier;

    @Column(name = "title", length = 2000, nullable = true)
    private String title;

    @Column(name = "public_at", nullable = true)
    private Date publicAt;

    @Column(name = "summary", length = 5000, nullable = true)
    private String summary;

    @Column(name = "url", length = 2000, nullable = true)
    private String url;

    @Column(name = "doi", length = 2000, nullable = true)
    private String doi;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPublicAt() {
        return publicAt;
    }

    public void setPublicAt(Date publicAt) {
        this.publicAt = publicAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    @Override
    public boolean equals(Object object) {
        if (object instanceof Proposal) {
            final Proposal other = (Proposal) object;
            return new EqualsBuilder()
                .append(identifier, other.identifier)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(identifier)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("identifier", identifier)
            .append("title", title)
            .append("publicAt", publicAt)
            .append("summary", summary)
            .append("url", summary)
            .append("doi", summary)
            .toString();
    }
}
