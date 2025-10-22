package eu.ill.visa.web.graphql.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.ill.visa.core.entity.DevicePool;
import eu.ill.visa.core.entity.enumerations.DeviceType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("DevicePool")
public class DevicePoolType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String name;
    private final String description;
    private final @NotNull DeviceType deviceType;
    private final @NotNull String computeIdentifier;
    private final Integer totalUnits;
    private final Long cloudId;

    public DevicePoolType(final DevicePool devicePool) {
        this.id = devicePool.getId();
        this.name = devicePool.getName();
        this.description = devicePool.getDescription();
        this.deviceType = devicePool.getDeviceType();
        this.computeIdentifier = devicePool.getComputeIdentifier();
        this.totalUnits = devicePool.getTotalUnits();
        this.cloudId = devicePool.getCloudId();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public String getComputeIdentifier() {
        return computeIdentifier;
    }

    public Integer getTotalUnits() {
        return totalUnits;
    }

    @JsonIgnore
    public Long getCloudId() {
        return cloudId;
    }
}
