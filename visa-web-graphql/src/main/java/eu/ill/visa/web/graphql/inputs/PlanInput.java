package eu.ill.visa.web.graphql.inputs;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Input;

@Input("PlanInput")
public class PlanInput {

    private @NotNull @AdaptToScalar(Scalar.Int.class) Long imageId;
    private @NotNull @AdaptToScalar(Scalar.Int.class) Long flavourId;
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
