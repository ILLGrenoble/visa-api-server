package eu.ill.visa.cloud.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CloudResourceInventory {

    private String resourceClass;
    private Long total;
    private Long reserved;
    private Long minUnit;
    private Long maxUnit;
    private Long stepSize;
    private Long allocationRatio;

    public String getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getReserved() {
        return reserved;
    }

    public void setReserved(Long reserved) {
        this.reserved = reserved;
    }

    public Long getMinUnit() {
        return minUnit;
    }

    @JsonProperty("min_unit")
    public void setMinUnit(Long minUnit) {
        this.minUnit = minUnit;
    }

    public Long getMaxUnit() {
        return maxUnit;
    }

    @JsonProperty("max_unit")
    public void setMaxUnit(Long maxUnit) {
        this.maxUnit = maxUnit;
    }

    public Long getStepSize() {
        return stepSize;
    }

    @JsonProperty("step_size")
    public void setStepSize(Long stepSize) {
        this.stepSize = stepSize;
    }

    public Long getAllocationRatio() {
        return allocationRatio;
    }

    @JsonProperty("allocation_ratio")
    public void setAllocationRatio(Long allocationRatio) {
        this.allocationRatio = allocationRatio;
    }
}
