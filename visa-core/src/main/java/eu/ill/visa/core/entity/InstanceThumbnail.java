package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.converter.Base64Converter;
import jakarta.persistence.*;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceThumbnail.getForInstanceUid", query = """
            SELECT it FROM InstanceThumbnail it
            WHERE it.instance.uid = :instanceUid
    """),
})
@Table(name = "instance_thumbnail", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"instance_id"}, name = "uk_instance_thumbnail_instance_id")
})
public class InstanceThumbnail extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Convert(converter = Base64Converter.class)
    @Column(name = "data", nullable = false, columnDefinition = "TEXT")
    private byte[] data;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private Instance instance;

    public InstanceThumbnail() {
    }

    public InstanceThumbnail(byte[] data) {
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] thumbnail) {
        this.data = thumbnail;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}
