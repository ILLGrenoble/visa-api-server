package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.domain.ProtocolStatus;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("ProtocolStatus")
public class ProtocolStatusType {

    private final @NotNull ImageProtocolType protocol;
    private final @NotNull Boolean active;

    public ProtocolStatusType(final ProtocolStatus status) {
        this.protocol = status.getProtocol() == null ? null : new ImageProtocolType(status.getProtocol());
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
