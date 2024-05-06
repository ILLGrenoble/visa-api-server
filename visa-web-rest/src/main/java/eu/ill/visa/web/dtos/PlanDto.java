package eu.ill.visa.web.dtos;

public class PlanDto {

    private Long id;
    private ImageDto image;
    private FlavourDto flavour;
    private Boolean preset;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ImageDto getImage() {
        return image;
    }

    public void setImage(ImageDto image) {
        this.image = image;
    }

    public FlavourDto getFlavour() {
        return flavour;
    }

    public void setFlavour(FlavourDto flavour) {
        this.flavour = flavour;
    }

    public Boolean getPreset() {
        return preset;
    }

    public void setPreset(Boolean preset) {
        this.preset = preset;
    }
}
