package eu.ill.visa.web.graphql.inputs;

import eu.ill.visa.core.entity.enumerations.DeviceType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.graphql.Input;

@Input("DevicePoolInput")
public class DevicePoolInput {

    @Size(min = 1, max = 250)
    private @NotNull String name;
    @Size(min = 1, max = 1000)
    private String description;
    private @NotNull DeviceType deviceType;
    private @NotNull String computeIdentifier;
    private String resourceClass;
    private Integer totalUnits;
    @Min(1)
    private @AdaptToScalar(Scalar.Int.class) Long cloudId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getComputeIdentifier() {
        return computeIdentifier;
    }

    public void setComputeIdentifier(String computeIdentifier) {
        this.computeIdentifier = computeIdentifier;
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(Integer totalUnits) {
        this.totalUnits = totalUnits;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }
}
