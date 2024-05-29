package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.domain.ProtocolStatus;

public class ProtocolStatusType {

    private final ImageProtocolType protocol;
    private final Boolean active;

    public ProtocolStatusType(final ProtocolStatus status) {
        this.protocol = new ImageProtocolType(status.getProtocol());
        this.active = status.getActive();
    }

    public ProtocolStatusType(final ImageProtocolType protocol, Boolean active) {
        this.protocol = protocol;
        this.active = active;
    }

    public ImageProtocolType getProtocol() {
        return protocol;
    }

    public Boolean getActive() {
        return active;
    }
}
