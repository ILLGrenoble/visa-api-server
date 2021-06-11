package eu.ill.visa.core.domain;

public class InstanceThumbnail extends Timestampable {

    private Long id;

    private byte[] data;

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
