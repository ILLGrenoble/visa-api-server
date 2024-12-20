package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@NamedQueries({
    @NamedQuery(name = "flavour.getById", query = """
            SELECT f
            FROM Flavour f
            LEFT JOIN f.cloudProviderConfiguration cpc
            WHERE f.id = :id
            AND f.deleted = false
            AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "flavour.getAll", query = """
            SELECT f
            FROM Flavour f
            LEFT JOIN f.cloudProviderConfiguration cpc
            WHERE f.deleted = false
            AND cpc.deletedAt IS NULL
            AND COALESCE(cpc.visible, true) = true
            ORDER BY f.cpu, f.memory, f.id
    """),
    @NamedQuery(name = "flavour.getAllForAdmin", query = """
            SELECT f
            FROM Flavour f
            LEFT JOIN FETCH f.cloudProviderConfiguration cpc
            WHERE f.deleted = false
            AND cpc.deletedAt IS NULL
            ORDER BY f.cpu, f.memory, f.id
    """),
    @NamedQuery(name = "flavour.countAllForAdmin", query = """
            SELECT count(distinct f.id)
            FROM Flavour f, Plan p, Instance i
            where p.flavour = f
            and i.plan = p
            and i.deletedAt IS NULL
    """),
})
@Table(name = "flavour")
public class Flavour extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @Column(name = "memory", nullable = false)
    private Integer memory;

    @Column(name = "cpu", precision = 2, nullable = false)
    private Float cpu;

    @Column(name = "compute_id", length = 250, nullable = false)
    private String computeId;

    @Column(name = "deleted", nullable = false, columnDefinition = "")
    private Boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "cloud_provider_configuration_id", foreignKey = @ForeignKey(name = "fk_cloud_provider_configuration_id"), nullable = true)
    private CloudProviderConfiguration cloudProviderConfiguration;

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

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public Float getCpu() {
        return cpu;
    }

    public void setCpu(Float cpu) {
        this.cpu = cpu;
    }

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public CloudProviderConfiguration getCloudProviderConfiguration() {
        return cloudProviderConfiguration;
    }

    public void setCloudProviderConfiguration(CloudProviderConfiguration cloudProviderConfiguration) {
        this.cloudProviderConfiguration = cloudProviderConfiguration;
    }

    @Transient
    public Long getCloudId() {
        return this.cloudProviderConfiguration == null ? null : this.cloudProviderConfiguration.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Flavour flavour = (Flavour) o;

        return new EqualsBuilder()
            .append(id, flavour.id)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }


    public static final class Builder {
        private Long id;
        private String name;
        private Integer memory;
        private Float cpu;
        private String computeId;

        public Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder memory(Integer memory) {
            this.memory = memory;
            return this;
        }

        public Builder cpu(Float cpu) {
            this.cpu = cpu;
            return this;
        }

        public Builder computeId(String computeId) {
            this.computeId = computeId;
            return this;
        }

        public Flavour build() {
            Flavour flavour = new Flavour();
            flavour.setId(id);
            flavour.setName(name);
            flavour.setMemory(memory);
            flavour.setCpu(cpu);
            flavour.setComputeId(computeId);
            return flavour;
        }
    }
}
