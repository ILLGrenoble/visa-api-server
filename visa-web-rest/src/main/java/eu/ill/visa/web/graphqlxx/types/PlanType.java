package eu.ill.visa.web.graphqlxx.types;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class PlanType {

    @AdaptToScalar(Scalar.Int.class)
    private Long id;
    private ImageType image;
    private FlavourType flavour;
    private Boolean preset;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ImageType getImage() {
        return image;
    }

    public void setImage(ImageType image) {
        this.image = image;
    }

    public FlavourType getFlavour() {
        return flavour;
    }

    public void setFlavour(FlavourType flavour) {
        this.flavour = flavour;
    }

    public Boolean getPreset() {
        return preset;
    }

    public void setPreset(Boolean preset) {
        this.preset = preset;
    }
}
