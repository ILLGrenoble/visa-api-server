package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.BookingRoleConfiguration;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("BookingRoleConfiguration")
public class BookingRoleConfigurationType {

    private final @NotNull Boolean autoAccept;
    private final @NotNull RoleType role;

    public BookingRoleConfigurationType(final BookingRoleConfiguration bookingRoleConfiguration) {
        this.autoAccept = bookingRoleConfiguration.getAutoAccept();
        this.role = new RoleType(bookingRoleConfiguration.getRole());
    }

    public Boolean getAutoAccept() {
        return autoAccept;
    }

    public RoleType getRole() {
        return role;
    }
}
