package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.Flavour;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

@Type("Flavour")
public class FlavourType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String name;
    private final @NotNull Integer memory;
    private final @NotNull Float cpu;
    private final @NotNull String computeId;
    private final List<FlavourDeviceType> devices;
    @AdaptToScalar(Scalar.Int.class)
    private final Long cloudId;

    public FlavourType(final Flavour flavour) {
        this.id = flavour.getId();
        this.name = flavour.getName();
        this.memory = flavour.getMemory();
        this.cpu = flavour.getCpu();
        this.computeId = flavour.getComputeId();
        this.devices = flavour.getDevices().stream().map(FlavourDeviceType::new).toList();
        this.cloudId = flavour.getCloudId();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getMemory() {
        return memory;
    }

    public Float getCpu() {
        return cpu;
    }

    public String getComputeId() {
        return computeId;
    }

    public List<FlavourDeviceType> getDevices() {
        return devices;
    }

    public Long getCloudId() {
        return cloudId;
    }
}
