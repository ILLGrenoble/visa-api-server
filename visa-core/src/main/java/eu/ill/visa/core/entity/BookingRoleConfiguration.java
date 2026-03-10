package eu.ill.visa.core.entity;

import jakarta.persistence.*;

@Entity
@NamedQueries({

})
@Table(name = "booking_role_configuration")
public class BookingRoleConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "auto_accept", nullable = false)
    private Boolean autoAccept;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_role_id"), nullable = true)
    private Role role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getAutoAccept() {
        return autoAccept;
    }

    public void setAutoAccept(Boolean autoAccept) {
        this.autoAccept = autoAccept;
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
        private Boolean autoAccept;
        private Role role;

        public Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder autoAccept(Boolean autoAccept) {
            this.autoAccept = autoAccept;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public BookingRoleConfiguration build() {
            BookingRoleConfiguration flavourRoleConfiguration = new BookingRoleConfiguration();
            flavourRoleConfiguration.setId(id);
            flavourRoleConfiguration.setAutoAccept(autoAccept);
            flavourRoleConfiguration.setRole(role);
            return flavourRoleConfiguration;
        }
    }
}
