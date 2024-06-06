package eu.ill.visa.cloud.providers.web.converters;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CloudLimitMixin {
    public @JsonProperty("maxTotalRamSize") Integer maxTotalRAMSize;
    public @JsonProperty("totalRamUsed") Integer totalRAMUsed;
    public Integer totalInstancesUsed;
    public Integer maxTotalInstances;
    public Integer maxTotalCores;
    public Integer totalCoresUsed;
}
