package eu.ill.visa.core.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
    @NamedQuery(name = "bookingConfiguration.getAll", query = """
        SELECT bc
        FROM BookingConfiguration bc
        LEFT JOIN bc.cloudProviderConfiguration cpc
        WHERE cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "bookingConfiguration.getById", query = """
        SELECT bc
        FROM BookingConfiguration bc
        LEFT JOIN bc.cloudProviderConfiguration cpc
        WHERE bc.id = :id
        AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "bookingConfiguration.getByCloudClientId", query = """
        SELECT bc
        FROM BookingConfiguration bc
        LEFT JOIN bc.cloudProviderConfiguration cpc
        WHERE cpc.id = :cloudClientId
        AND cpc.deletedAt IS NULL
    """),
})
@Table(name = "booking_configuration")
public class BookingConfiguration extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "max_instances_per_reservation", nullable = true)
    private Long maxInstancesPerReservation;

    @Column(name = "max_days_in_advance", nullable = true)
    private Long maxDaysInAdvance;

    @Column(name = "max_days_reservation", nullable = true)
    private Long maxDaysReservation;

    @ManyToOne
    @JoinColumn(name = "cloud_provider_configuration_id", foreignKey = @ForeignKey(name = "fk_cloud_provider_configuration_id"), nullable = true)
    private CloudProviderConfiguration cloudProviderConfiguration;

    @ManyToMany(fetch =  FetchType.EAGER)
    @JoinTable(
        name = "booking_configuration_flavour",
        joinColumns = @JoinColumn(name = "booking_configuration_id", foreignKey = @ForeignKey(name = "fk_booking_configuration_id")),
        inverseJoinColumns = @JoinColumn(name = "flavour_id", foreignKey = @ForeignKey(name = "fk_flavour_id"))
    )
    private List<Flavour> flavours = new ArrayList<>();

    @ManyToMany(fetch =  FetchType.EAGER)
    @JoinTable(
        name = "booking_configuration_role",
        joinColumns = @JoinColumn(name = "booking_configuration_id", foreignKey = @ForeignKey(name = "fk_booking_configuration_id")),
        inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_role_id"))
    )
    private List<Role> roles = new ArrayList<>();

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_configuration_id", foreignKey = @ForeignKey(name = "fk_booking_configuration_id"), nullable = false)
    private List<BookingFlavourRoleConfiguration> flavourRoleConfigurations = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getMaxInstancesPerReservation() {
        return maxInstancesPerReservation;
    }

    public void setMaxInstancesPerReservation(Long maxInstancesPerReservation) {
        this.maxInstancesPerReservation = maxInstancesPerReservation;
    }

    public Long getMaxDaysInAdvance() {
        return maxDaysInAdvance;
    }

    public void setMaxDaysInAdvance(Long maxDaysInAdvance) {
        this.maxDaysInAdvance = maxDaysInAdvance;
    }

    public Long getMaxDaysReservation() {
        return maxDaysReservation;
    }

    public void setMaxDaysReservation(Long maxDaysReservation) {
        this.maxDaysReservation = maxDaysReservation;
    }

    public CloudProviderConfiguration getCloudProviderConfiguration() {
        return cloudProviderConfiguration;
    }

    public void setCloudProviderConfiguration(CloudProviderConfiguration cloudProviderConfiguration) {
        this.cloudProviderConfiguration = cloudProviderConfiguration;
    }

    public List<Flavour> getFlavours() {
        return flavours;
    }

    public void setFlavours(List<Flavour> flavours) {
        this.flavours = flavours;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<BookingFlavourRoleConfiguration> getFlavourRoleConfigurations() {
        return flavourRoleConfigurations;
    }

    public void setFlavourRoleConfigurations(List<BookingFlavourRoleConfiguration> flavourRoleConfigurations) {
        this.flavourRoleConfigurations = flavourRoleConfigurations;
    }

    @Transient
    public Long getCloudId() {
        return this.cloudProviderConfiguration == null ? null : this.cloudProviderConfiguration.getId();
    }

    public static Builder Builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private boolean enabled;
        private Long maxInstancesPerReservation;
        private Long maxDaysInAdvance;
        private Long maxDaysReservation;
        private CloudProviderConfiguration cloudProviderConfiguration;
        private List<Flavour> flavours;
        private List<Role> roles;
        private List<BookingFlavourRoleConfiguration> flavourRoleConfigurations;

        public Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder maxInstancesPerReservation(Long maxInstancesPerReservation) {
            this.maxInstancesPerReservation = maxInstancesPerReservation;
            return this;
        }

        public Builder maxDaysInAdvance(Long maxDaysInAdvance) {
            this.maxDaysInAdvance = maxDaysInAdvance;
            return this;
        }

        public Builder maxDaysReservation(Long maxDaysReservation) {
            this.maxDaysReservation = maxDaysReservation;
            return this;
        }

        public Builder cloudProviderConfiguration(CloudProviderConfiguration cloudProviderConfiguration) {
            this.cloudProviderConfiguration = cloudProviderConfiguration;
            return this;
        }

        public Builder flavours(List<Flavour> flavours) {
            this.flavours = flavours;
            return this;
        }

        public Builder roles(List<Role> roles) {
            this.roles = roles;
            return this;
        }

        public Builder flavourRoleConfigurations(List<BookingFlavourRoleConfiguration> flavourRoleConfigurations) {
            this.flavourRoleConfigurations = flavourRoleConfigurations;
            return this;
        }

        public BookingConfiguration build() {
            BookingConfiguration devicePool = new BookingConfiguration();
            devicePool.setId(id);
            devicePool.setEnabled(enabled);
            devicePool.setMaxInstancesPerReservation(maxInstancesPerReservation);
            devicePool.setMaxDaysInAdvance(maxDaysInAdvance);
            devicePool.setMaxDaysReservation(maxDaysReservation);
            devicePool.setCloudProviderConfiguration(cloudProviderConfiguration);
            devicePool.setFlavours(flavours);
            devicePool.setRoles(roles);
            devicePool.setFlavourRoleConfigurations(flavourRoleConfigurations);
            return devicePool;
        }
    }
}
