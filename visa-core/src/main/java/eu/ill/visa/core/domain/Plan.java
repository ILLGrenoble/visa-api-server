package eu.ill.visa.core.domain;

import jakarta.persistence.Transient;
import java.util.Date;

public class Plan extends Timestampable {

    private Long id;

    private Image image;

    private Flavour flavour;

    private Boolean preset;

    private Date deletedAt;

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

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Transient
    public void setDeleted(boolean value) {
        if (value) {
            this.deletedAt = new Date();

        } else {
            this.deletedAt = null;
        }
    }

    @Transient
    public Long getCloudId() {
        return this.image.getCloudId();
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
