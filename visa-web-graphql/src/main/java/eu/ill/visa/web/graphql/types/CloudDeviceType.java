package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.domain.CloudDevice;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("CloudDevice")
public class CloudDeviceType {

    private final @NotNull String identifier;
    private final @NotNull CloudDevice.Type type;

    public CloudDeviceType(final CloudDevice cloudDevice) {
        this.identifier = cloudDevice.getIdentifier();
        this.type = cloudDevice.getType();
    }

    public String getIdentifier() {
        return identifier;
    }

    public CloudDevice.Type getType() {
        return type;
    }
}
