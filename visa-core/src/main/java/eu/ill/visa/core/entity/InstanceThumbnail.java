package eu.ill.visa.core.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "instance_thumbnail")
public class InstanceThumbnail extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

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
