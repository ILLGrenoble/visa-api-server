package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.converter.Base64Converter;
import jakarta.persistence.*;

@Entity
@NamedQueries({
    @NamedQuery(name = "instanceThumbnail.getForInstanceId", query = """
        SELECT it
        FROM InstanceThumbnail it
        WHERE it.instanceId = :instanceId
    """),
    @NamedQuery(name = "instanceThumbnail.getForInstanceUid", query = """
        SELECT it
        FROM InstanceThumbnail it
        JOIN Instance i ON it.instanceId = i.id
        WHERE i.uid = :instanceUid
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

    @Column(name = "instance_id", nullable = false)
    @JoinColumn(name = "instance_id")
    private Long instanceId;

    public InstanceThumbnail() {
    }

    public InstanceThumbnail(Long instanceId) {
        this.instanceId = instanceId;
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

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Entity mapping used to force foreign key constraints onto instance_id. This entity is not used elsewhere.
     */
    @Entity
    @Table(name = "instance_thumbnail")
    private static class InstanceThumbnailInner {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
        private Instance instance;
    }
}
