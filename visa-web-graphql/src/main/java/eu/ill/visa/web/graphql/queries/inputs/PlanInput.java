package eu.ill.visa.web.graphql.queries.inputs;

public class PlanInput {

    private Long imageId;

    private Long flavourId;

    private Boolean preset;

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
