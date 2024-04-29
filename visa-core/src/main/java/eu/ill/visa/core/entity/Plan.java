package eu.ill.visa.core.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "plan.getById", query = """
            SELECT p
            FROM Plan p
            LEFT JOIN p.image i
            LEFT JOIN p.flavour f
            LEFT JOIN i.cloudProviderConfiguration cpc
            WHERE p.id = :id
            AND i.deleted = false
            AND f.deleted = false
            AND p.deletedAt IS null
            AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "plan.getAll", query = """
            SELECT p
            FROM Plan p
            LEFT JOIN p.image i
            LEFT JOIN p.flavour f
            LEFT JOIN i.cloudProviderConfiguration cpc
            WHERE i.deleted = false
            AND f.deleted = false
            AND i.visible = true
            AND p.deletedAt IS null
            AND cpc.deletedAt IS NULL
            AND COALESCE(cpc.visible, true) = true
            ORDER BY p.id
    """),
    @NamedQuery(name = "plan.getAllForAdmin", query = """
            SELECT p
            FROM Plan p
            LEFT JOIN p.image i
            LEFT JOIN p.flavour f
            LEFT JOIN i.cloudProviderConfiguration cpc
            WHERE i.deleted = false
            AND f.deleted = false
            AND p.deletedAt IS null
            AND cpc.deletedAt IS NULL
            ORDER BY p.id
    """),
    @NamedQuery(name = "plan.countAllForAdmin", query = """
            SELECT COUNT(p)
            FROM Plan p
            LEFT JOIN p.image i
            LEFT JOIN p.flavour f
            LEFT JOIN i.cloudProviderConfiguration cpc
            WHERE i.deleted = false
            AND f.deleted = false
            AND p.deletedAt IS null
            AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "plan.getAllForInstrumentIds", query = """
            SELECT DISTINCT p
            FROM Plan p
            LEFT JOIN p.image i
            LEFT JOIN p.flavour f
            LEFT JOIN i.cloudProviderConfiguration cpc
            LEFT OUTER JOIN FlavourLimit fl ON fl.flavour.id = p.flavour.id AND fl.objectType = 'INSTRUMENT'
            WHERE i.deleted = false
            AND f.deleted = false
            AND p.deletedAt IS null
            AND cpc.deletedAt IS NULL
            AND i.visible = true
            AND COALESCE(cpc.visible, true) = true
            AND (fl.objectId IN :instrumentIds OR fl.objectId IS NULL)
            ORDER BY p.id
    """),
    @NamedQuery(name = "plan.getAllForExperimentIds", query = """
            SELECT DISTINCT p
            FROM Plan p
            LEFT JOIN p.image i
            LEFT JOIN p.flavour f
            LEFT JOIN i.cloudProviderConfiguration cpc
            LEFT OUTER JOIN FlavourLimit fl ON fl.flavour.id = p.flavour.id AND fl.objectType = 'INSTRUMENT'
            LEFT OUTER JOIN Experiment e ON e.instrument.id = fl.objectId
            WHERE i.deleted = false
            AND f.deleted = false
            AND p.deletedAt IS null
            AND cpc.deletedAt IS NULL
            AND i.visible = true
            AND COALESCE(cpc.visible, true) = true
            AND (e.id IN :experimentIds OR fl.objectId IS NULL)
            ORDER BY p.id
    """),
    @NamedQuery(name = "plan.getAllForUserAndExperimentIds", query = """
            SELECT DISTINCT p
            FROM Plan p
            LEFT JOIN p.image i
            LEFT JOIN p.flavour f
            LEFT JOIN i.cloudProviderConfiguration cpc
            LEFT OUTER JOIN FlavourLimit ifl ON ifl.flavour.id = p.flavour.id AND ifl.objectType = 'INSTRUMENT'
            LEFT OUTER JOIN Experiment e ON e.instrument.id = ifl.objectId
            LEFT OUTER JOIN InstrumentScientist ir on ir.instrument.id = ifl.objectId
            LEFT OUTER JOIN FlavourLimit rfl ON rfl.flavour.id = p.flavour.id AND rfl.objectType = 'ROLE'
            LEFT OUTER JOIN UserRole ur on ur.role.id = rfl.objectId
            LEFT OUTER JOIN Role r on r.id = ur.role.id
            WHERE i.deleted = false
            AND f.deleted = false
            AND p.deletedAt IS null
            AND cpc.deletedAt IS NULL
            AND i.visible = true
            AND COALESCE(cpc.visible, true) = true
            AND r.groupDeletedAt IS NULL
            AND (ir.user = :user OR ur.user = :user OR e.id IN :experimentIds OR (ifl.objectId IS NULL AND rfl.objectId IS NULL))
            ORDER BY p.id
    """),
    @NamedQuery(name = "plan.getAllForAllInstruments", query = """
            SELECT DISTINCT p
            FROM Plan p
            LEFT JOIN p.image i
            LEFT JOIN p.flavour f
            LEFT JOIN i.cloudProviderConfiguration cpc
            LEFT OUTER JOIN FlavourLimit fl ON fl.flavour.id = p.flavour.id AND fl.objectType = 'INSTRUMENT'
            WHERE i.deleted = false
            AND f.deleted = false
            AND p.deletedAt IS null
            AND cpc.deletedAt IS NULL
            AND i.visible = true
            AND COALESCE(cpc.visible, true) = true
            AND fl.objectId IS NULL
            ORDER BY p.id
    """),
    @NamedQuery(name = "plan.getAllForUserAndAllInstruments", query = """
            SELECT DISTINCT p
            FROM Plan p
            LEFT JOIN p.image i
            LEFT JOIN p.flavour f
            LEFT JOIN i.cloudProviderConfiguration cpc
            LEFT OUTER JOIN FlavourLimit ifl ON ifl.flavour.id = p.flavour.id AND ifl.objectType = 'INSTRUMENT'
            LEFT OUTER JOIN InstrumentScientist ir on ir.instrument.id = ifl.objectId
            LEFT OUTER JOIN FlavourLimit rfl ON rfl.flavour.id = p.flavour.id AND rfl.objectType = 'ROLE'
            LEFT OUTER JOIN UserRole ur on ur.role.id = rfl.objectId
            LEFT OUTER JOIN Role r on r.id = ur.role.id
            WHERE i.deleted = false
            AND f.deleted = false
            AND p.deletedAt IS null
            AND cpc.deletedAt IS NULL
            AND i.visible = true
            AND COALESCE(cpc.visible, true) = true
            AND r.groupDeletedAt IS NULL
            AND (ir.user = :user OR ur.user = :user OR (ifl.objectId IS NULL AND rfl.objectId IS NULL))
            ORDER BY p.id
    """),
})
@Table(name = "plan")
public class Plan extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "image_id", foreignKey = @ForeignKey(name = "fk_image_id"))
    private Image image;

    @ManyToOne
    @JoinColumn(name = "flavour_id", foreignKey = @ForeignKey(name = "fk_flavour_id"))
    private Flavour flavour;

    @Column(name = "preset", nullable = false)
    private Boolean preset;

    @Column(name = "deleted_at", nullable = true)
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
