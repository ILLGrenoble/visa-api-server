package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.BookingConfiguration;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

@Type("BookingConfiguration")
public class BookingConfigurationType {

    private final @NotNull Boolean enabled;
    @AdaptToScalar(Scalar.Int.class)
    private final Long maxInstancesPerReservation;
    @AdaptToScalar(Scalar.Int.class)
    private final Long maxDaysInAdvance;
    @AdaptToScalar(Scalar.Int.class)
    private final Long maxDaysReservation;
    private final Long cloudId;
    private final @NotNull List<FlavourType> flavours;
    private final @NotNull List<RoleType> roles;
    private final @NotNull List<BookingFlavourRoleConfigurationType> flavourRoleConfigurations;

    public BookingConfigurationType(final BookingConfiguration bookingConfiguration) {
        this.enabled = bookingConfiguration.isEnabled();
        this.maxInstancesPerReservation = bookingConfiguration.getMaxInstancesPerReservation();
        this.maxDaysInAdvance = bookingConfiguration.getMaxDaysInAdvance();
        this.maxDaysReservation = bookingConfiguration.getMaxDaysReservation();
        this.cloudId = bookingConfiguration.getCloudId();
        this.flavours = bookingConfiguration.getFlavours().stream().map(FlavourType::new).toList();
        this.roles = bookingConfiguration.getRoles().stream().map(RoleType::new).toList();
        this.flavourRoleConfigurations = bookingConfiguration.getFlavourRoleConfigurations().stream().map(BookingFlavourRoleConfigurationType::new).toList();
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Long getMaxInstancesPerReservation() {
        return maxInstancesPerReservation;
    }

    public Long getMaxDaysInAdvance() {
        return maxDaysInAdvance;
    }

    public Long getMaxDaysReservation() {
        return maxDaysReservation;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public List<FlavourType> getFlavours() {
        return flavours;
    }

    public List<RoleType> getRoles() {
        return roles;
    }

    public List<BookingFlavourRoleConfigurationType> getFlavourRoleConfigurations() {
        return flavourRoleConfigurations;
    }
}
