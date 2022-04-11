package eu.ill.visa.web.bundles.graphql.queries.inputs;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UserInput {

    @NotNull
    @Min(0)
    private Integer instanceQuota;

    @NotNull
    private Boolean admin;

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
}
