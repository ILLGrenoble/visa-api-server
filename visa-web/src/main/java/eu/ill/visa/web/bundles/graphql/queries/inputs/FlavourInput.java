package eu.ill.visa.web.bundles.graphql.queries.inputs;

import eu.ill.visa.web.bundles.graphql.validation.Cloud;

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

    @NotNull
    @Min(1)
    private Integer credits;

    @Cloud(type = "flavour")
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

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
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
