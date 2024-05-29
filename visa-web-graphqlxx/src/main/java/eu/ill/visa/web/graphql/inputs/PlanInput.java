package eu.ill.visa.web.graphql.inputs;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Input;

@Input("PlanInput")
public class PlanInput {

    private @NotNull Long imageId;
    private @NotNull Long flavourId;
    private @NotNull Boolean preset;

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Long getFlavourId() {
        return flavourId;
    }

    public void setFlavourId(Long flavourId) {
        this.flavourId = flavourId;
    }

    public Boolean getPreset() {
        return preset;
    }

    public void setPreset(Boolean preset) {
        this.preset = preset;
    }
}
