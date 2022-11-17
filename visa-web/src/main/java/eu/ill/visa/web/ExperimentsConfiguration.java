package eu.ill.visa.web;

import javax.validation.constraints.NotNull;

public class ExperimentsConfiguration {

    @NotNull
    private Boolean openDataIncluded;

    public Boolean getOpenDataIncluded() {
        return openDataIncluded;
    }

    public void setOpenDataIncluded(Boolean openDataIncluded) {
        this.openDataIncluded = openDataIncluded;
    }
}
