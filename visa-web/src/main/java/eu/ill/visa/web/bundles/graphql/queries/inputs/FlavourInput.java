package eu.ill.visa.web.bundles.graphql.queries.inputs;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class FlavourInput {

    @NotNull
    @Size(min = 1, max = 250)
    private String name;

    @NotNull
    @Min(1)
    private Integer memory;

    @NotNull
    private Float cpu;

    private Long cloudId;

    @NotNull
    private String computeId;

    private List<Long> instrumentIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public Float getCpu() {
        return cpu;
    }

    public void setCpu(Float cpu) {
        this.cpu = cpu;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
    }

    public List<Long> getInstrumentIds() {
        return instrumentIds;
    }

    public void setInstrumentIds(List<Long> instrumentIds) {
        this.instrumentIds = instrumentIds;
    }
}
