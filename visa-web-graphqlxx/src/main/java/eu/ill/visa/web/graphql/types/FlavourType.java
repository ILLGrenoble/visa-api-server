package eu.ill.visa.web.graphql.types;

import com.fasterxml.jackson.annotation.*;
import eu.ill.visa.core.entity.Flavour;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import org.eclipse.microprofile.graphql.Type;

@Type("Flavour")
public class FlavourType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String name;
    private final Integer memory;
    private final Float cpu;
    private final String computeId;
    private final Long cloudId;

    public FlavourType(final Flavour flavour) {
        this.id = flavour.getId();
        this.name = flavour.getName();
        this.memory = flavour.getMemory();
        this.cpu = flavour.getCpu();
        this.computeId = flavour.getComputeId();
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

    @JsonIgnore
    public Long getCloudId() {
        return cloudId;
    }
}
