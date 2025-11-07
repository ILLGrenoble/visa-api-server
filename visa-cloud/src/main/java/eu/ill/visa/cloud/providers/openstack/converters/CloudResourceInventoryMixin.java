package eu.ill.visa.cloud.providers.openstack.converters;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class CloudResourceInventoryMixin {

    private String resourceClass;
    private Long total;
    private Long reserved;
    private @JsonProperty("min_unit") Long minUnit;
    private @JsonProperty("max_unit") Long maxUnit;
    private @JsonProperty("step_size") Long stepSize;
    private @JsonProperty("allocation_ratio") Long allocationRatio;
}
