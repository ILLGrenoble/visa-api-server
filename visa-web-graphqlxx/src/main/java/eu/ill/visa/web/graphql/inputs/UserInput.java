package eu.ill.visa.web.graphql.inputs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Input;

import java.util.List;

@Input("UserInput")
public class UserInput {

    @Min(0)
    private @NotNull Integer instanceQuota;
    private @NotNull Boolean admin;
    private @NotNull Boolean guest;
    private String guestExpiresAt;
    private List<Long> groupIds;

    public Integer getInstanceQuota() {
        return instanceQuota;
    }

    public void setInstanceQuota(Integer instanceQuota) {
        this.instanceQuota = instanceQuota;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public Boolean isAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Boolean getGuest() {
        return guest;
    }

    public Boolean isGuest() {
        return guest;
    }

    public void setGuest(Boolean guest) {
        this.guest = guest;
    }

    public String getGuestExpiresAt() {
        return guestExpiresAt;
    }

    public void setGuestExpiresAt(String guestExpiresAt) {
        this.guestExpiresAt = guestExpiresAt;
    }

    public List<Long> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<Long> groupIds) {
        this.groupIds = groupIds;
    }
}
