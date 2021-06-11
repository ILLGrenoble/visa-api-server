package eu.ill.visa.core.domain;

public class Plan extends Timestampable {

    private Long id;

    private Image image;

    private Flavour flavour;

    private Boolean preset;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Flavour getFlavour() {
        return flavour;
    }

    public void setFlavour(Flavour flavour) {
        this.flavour = flavour;
    }

    public Boolean getPreset() {
        return preset;
    }

    public void setPreset(Boolean preset) {
        this.preset = preset;
    }

    public static final class Builder {
        private Long id;
        private Image image;
        private Flavour flavour;
        private Boolean preset = false;

        public Builder() {
        }

        public static Plan.Builder builder() {
            return new Plan.Builder();
        }

        public Plan.Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Plan.Builder image(Image image) {
            this.image = image;
            return this;
        }

        public Plan.Builder flavour(Flavour flavour) {
            this.flavour = flavour;
            return this;
        }

        public Plan.Builder preset(Boolean preset) {
            this.preset = preset;
            return this;
        }

        public Plan build() {
            Plan plan = new Plan();
            plan.setId(id);
            plan.setImage(image);
            plan.setFlavour(flavour);
            plan.setPreset(preset);
            return plan;
        }
    }
}
