package eu.ill.visa.core.entity;

import eu.ill.visa.core.domain.SimpleDuration;
import eu.ill.visa.core.entity.converter.SimpleDurationConverter;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@NamedQueries({
    @NamedQuery(name = "flavourRoleLifetime.getAll", query = """
        SELECT frl
        FROM FlavourRoleLifetime  frl
        LEFT JOIN frl.flavour f
        LEFT JOIN f.cloudProviderConfiguration cpc
        LEFT OUTER JOIN frl.role r ON r.id = frl.role.id AND r.groupDeletedAt IS NULL
        WHERE f.deleted = false
        AND cpc.deletedAt IS NULL
        ORDER BY frl.id

    """),
    @NamedQuery(name = "flavourRoleLifetime.getAllByFlavourId", query = """
        SELECT frl
        FROM FlavourRoleLifetime  frl
        LEFT JOIN frl.flavour f
        LEFT JOIN f.cloudProviderConfiguration cpc
        LEFT OUTER JOIN frl.role r ON r.id = frl.role.id AND r.groupDeletedAt IS NULL
        WHERE f.deleted = false
        AND cpc.deletedAt IS NULL
        AND frl.flavour.id = :flavourId
        ORDER BY frl.id
    """),
    @NamedQuery(name = "flavourRoleLifetime.getAllByFlavourIds", query = """
        SELECT frl
        FROM FlavourRoleLifetime  frl
        LEFT JOIN frl.flavour f
        LEFT JOIN f.cloudProviderConfiguration cpc
        LEFT OUTER JOIN frl.role r ON r.id = frl.role.id AND r.groupDeletedAt IS NULL
        WHERE f.deleted = false
        AND cpc.deletedAt IS NULL
        AND frl.flavour.id IN :flavourIds
        ORDER BY frl.id
    """),
    @NamedQuery(name = "flavourRoleLifetime.getById", query = """
        SELECT frl
        FROM FlavourRoleLifetime  frl
        LEFT JOIN frl.flavour f
        LEFT JOIN f.cloudProviderConfiguration cpc
        LEFT OUTER JOIN frl.role r ON r.id = frl.role.id AND r.groupDeletedAt IS NULL
        WHERE f.deleted = false
        AND cpc.deletedAt IS NULL
        AND frl.id = :id
    """),
})
@Table(name = "flavour_role_lifetime",
    uniqueConstraints= @UniqueConstraint(columnNames={"flavour_id", "role_id"}))
public class FlavourRoleLifetime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flavour_id", foreignKey = @ForeignKey(name = "fk_flavour_id"), nullable = false)
    private Flavour flavour;

    // Role can be null: if null then it is the default lifetime for the flavour
    @ManyToOne(optional = true)
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_role_id"), nullable = true)
    private Role role;

    @Convert(converter = SimpleDurationConverter.class)
    @Column(name = "duration_minutes", nullable = false)
    private SimpleDuration duration;

    public FlavourRoleLifetime() {
    }

    public FlavourRoleLifetime(Long id, Flavour flavour, Role role, Long lifetimeMinutes) {
        this.id = id;
        this.flavour = flavour;
        this.role = role;
        this.duration = new SimpleDuration(lifetimeMinutes);
    }

    public FlavourRoleLifetime(Long id, Flavour flavour, Long lifetimeMinutes) {
        this.id = id;
        this.flavour = flavour;
        this.role =  null;
        this.duration = new SimpleDuration(lifetimeMinutes);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Flavour getFlavour() {
        return flavour;
    }

    public void setFlavour(Flavour flavour) {
        this.flavour = flavour;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public SimpleDuration getDuration() {
        return duration;
    }

    public void setDuration(SimpleDuration duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FlavourRoleLifetime that = (FlavourRoleLifetime) o;

        return new EqualsBuilder()
            .append(flavour, that.flavour)
            .append(role, that.role)
            .append(duration, that.duration)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(flavour)
            .append(role)
            .append(duration)
            .toHashCode();
    }
}
