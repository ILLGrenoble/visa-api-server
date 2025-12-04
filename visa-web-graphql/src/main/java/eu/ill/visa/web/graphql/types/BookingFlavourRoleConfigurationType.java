package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.BookingFlavourRoleConfiguration;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("BookingFlavourRoleConfiguration")
public class BookingFlavourRoleConfigurationType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long maxInstancesPerReservation;
    @AdaptToScalar(Scalar.Int.class)
    private final Long maxDaysReservation;
    private final @NotNull FlavourType flavour;
    private final RoleType role;

    public BookingFlavourRoleConfigurationType(final BookingFlavourRoleConfiguration bookingFlavourRoleConfiguration) {
        this.maxInstancesPerReservation = bookingFlavourRoleConfiguration.getMaxInstancesPerReservation();
        this.maxDaysReservation = bookingFlavourRoleConfiguration.getMaxDaysReservation();
        this.flavour = new FlavourType(bookingFlavourRoleConfiguration.getFlavour());
        this.role = new RoleType(bookingFlavourRoleConfiguration.getRole());
    }

    public Long getMaxInstancesPerReservation() {
        return maxInstancesPerReservation;
    }

    public Long getMaxDaysReservation() {
        return maxDaysReservation;
    }

    public FlavourType getFlavour() {
        return flavour;
    }

    public RoleType getRole() {
        return role;
    }
}
