package eu.ill.visa.core.entity;

import jakarta.persistence.*;

@Entity
@NamedQueries({

})
@Table(name = "booking_flavour_role_configuration")
public class BookingFlavourRoleConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "max_instances_per_reservation", nullable = true)
    private Long maxInstancesPerReservation;

    @Column(name = "max_days_reservation", nullable = true)
    private Long maxDaysReservation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flavour_id", foreignKey = @ForeignKey(name = "fk_flavour_id"))
    private Flavour flavour;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_role_id"), nullable = true)
    private Role role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaxInstancesPerReservation() {
        return maxInstancesPerReservation;
    }

    public void setMaxInstancesPerReservation(Long maxInstancesPerReservation) {
        this.maxInstancesPerReservation = maxInstancesPerReservation;
    }

    public Long getMaxDaysReservation() {
        return maxDaysReservation;
    }

    public void setMaxDaysReservation(Long maxDaysReservation) {
        this.maxDaysReservation = maxDaysReservation;
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

    public static Builder Builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private Long maxInstancesPerReservation;
        private Long maxDaysReservation;
        private Flavour flavour;
        private Role role;

        public Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder maxInstancesPerReservation(Long maxInstancesPerReservation) {
            this.maxInstancesPerReservation = maxInstancesPerReservation;
            return this;
        }

        public Builder maxDaysReservation(Long maxDaysReservation) {
            this.maxDaysReservation = maxDaysReservation;
            return this;
        }

        public Builder flavour(Flavour flavour) {
            this.flavour = flavour;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public BookingFlavourRoleConfiguration build() {
            BookingFlavourRoleConfiguration devicePool = new BookingFlavourRoleConfiguration();
            devicePool.setId(id);
            devicePool.setMaxInstancesPerReservation(maxInstancesPerReservation);
            devicePool.setMaxDaysReservation(maxDaysReservation);
            devicePool.setFlavour(flavour);
            devicePool.setRole(role);
            return devicePool;
        }
    }
}
