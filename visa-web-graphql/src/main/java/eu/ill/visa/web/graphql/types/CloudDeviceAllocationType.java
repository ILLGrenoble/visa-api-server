package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.domain.CloudDeviceAllocation;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("CloudDeviceAllocation")
public class CloudDeviceAllocationType {

    private final @NotNull CloudDeviceType device;
    private final @NotNull Integer unitCount;

    public CloudDeviceAllocationType(final CloudDeviceAllocation cloudDeviceAllocation) {
        this.device = new CloudDeviceType(cloudDeviceAllocation.getDevice());
        this.unitCount = cloudDeviceAllocation.getUnitCount();
    }

    public CloudDeviceType getDevice() {
        return device;
    }

    public Integer getUnitCount() {
        return unitCount;
    }
}
