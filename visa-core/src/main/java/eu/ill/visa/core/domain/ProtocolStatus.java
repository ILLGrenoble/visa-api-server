package eu.ill.visa.core.domain;

public class ProtocolStatus {

    private ImageProtocol protocol;
    private Boolean active;

    public ProtocolStatus() {

    }

    public ProtocolStatus(ImageProtocol protocol, Boolean active) {
        this.protocol = protocol;
        this.active = active;
    }

    public ImageProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ImageProtocol protocol) {
        this.protocol = protocol;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
