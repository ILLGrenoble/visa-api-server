package eu.ill.visa.web.graphql.queries.inputs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class UserInput {

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @NotNull
    @Min(0)
    private Integer instanceQuota;

    @NotNull
    private Boolean admin;

    @NotNull
    private Boolean guest;

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
