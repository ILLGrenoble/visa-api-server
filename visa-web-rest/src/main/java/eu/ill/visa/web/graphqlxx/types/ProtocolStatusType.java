package eu.ill.visa.web.graphqlxx.types;

public class ProtocolStatusType {

    private ImageProtocolType protocol;
    private Boolean active;

    public ImageProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ImageProtocolType protocol) {
        this.protocol = protocol;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
