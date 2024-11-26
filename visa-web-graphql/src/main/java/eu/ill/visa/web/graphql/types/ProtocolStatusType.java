package eu.ill.visa.web.graphql.types;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("ProtocolStatus")
public class ProtocolStatusType {

    private final @NotNull ImageProtocolType protocol;
    private final @NotNull Boolean active;

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
