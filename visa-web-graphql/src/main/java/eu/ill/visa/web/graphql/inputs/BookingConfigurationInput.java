package eu.ill.visa.web.graphql.inputs;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Input;

import java.util.List;

@Input("BookingConfigurationInput")
public class BookingConfigurationInput {

    private @NotNull Boolean enabled;
    private @AdaptToScalar(Scalar.Int.class) @Min(1) Long maxInstancesPerReservation;
    private @AdaptToScalar(Scalar.Int.class) @Min(1) Long maxDaysReservation;
    private @AdaptToScalar(Scalar.Int.class) Long cloudId;
    private @AdaptToScalar(Scalar.Int.class) @NotNull List<Long> flavourIds;
    private @NotNull List<BookingRoleConfigurationInput> roleConfigurations;
    private @NotNull List<BookingFlavourRoleConfigurationInput> flavourRoleConfigurations;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    public Long getCloudId() {
        return cloudId;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public List<Long> getFlavourIds() {
        return flavourIds;
    }

    public void setFlavourIds(List<Long> flavourIds) {
        this.flavourIds = flavourIds;
    }

    public List<BookingRoleConfigurationInput> getRoleConfigurations() {
        return roleConfigurations;
    }

    public void setRoleConfigurations(List<BookingRoleConfigurationInput> roleConfigurations) {
        this.roleConfigurations = roleConfigurations;
    }

    public List<BookingFlavourRoleConfigurationInput> getFlavourRoleConfigurations() {
        return flavourRoleConfigurations;
    }

    public void setFlavourRoleConfigurations(List<BookingFlavourRoleConfigurationInput> flavourRoleConfigurations) {
        this.flavourRoleConfigurations = flavourRoleConfigurations;
    }

    public static final class BookingFlavourRoleConfigurationInput {
        private @AdaptToScalar(Scalar.Int.class) @NotNull Long flavourId;
        private @AdaptToScalar(Scalar.Int.class) Long roleId;
        private @AdaptToScalar(Scalar.Int.class) @Min(1) Long maxInstancesPerReservation;
        private @AdaptToScalar(Scalar.Int.class) @Min(1) Long maxDaysReservation;

        public Long getFlavourId() {
            return flavourId;
        }

        public void setFlavourId(Long flavourId) {
            this.flavourId = flavourId;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
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
    }

    public static final class BookingRoleConfigurationInput {
        private @AdaptToScalar(Scalar.Int.class) @NotNull Long roleId;
        private @NotNull Boolean autoAccept;

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public Boolean getAutoAccept() {
            return autoAccept;
        }

        public void setAutoAccept(Boolean autoAccept) {
            this.autoAccept = autoAccept;
        }
    }

}
