package eu.ill.visa.web.bundles.graphql.queries.inputs;

import eu.ill.visa.web.bundles.graphql.validation.Cloud;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UpdateFlavourInput {

    @NotNull
    @Size(min = 1, max = 250)
    private String name;

    @NotNull
    @Min(1)
    private Integer memory;

    @NotNull
    private Float cpu;

    @Cloud(type = "flavour")
    @NotNull
    private String computeId;

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

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
    }
}
