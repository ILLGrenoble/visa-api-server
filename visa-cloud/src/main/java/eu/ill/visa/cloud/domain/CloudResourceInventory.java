package eu.ill.visa.cloud.domain;

public class CloudResourceInventory {

    private CloudResourceClass resourceClass;
    private Long total;
    private Long reserved;
    private Long minUnit;
    private Long maxUnit;
    private Long stepSize;
    private Long allocationRatio;

    public CloudResourceClass getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(CloudResourceClass resourceClass) {
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

    public void setMinUnit(Long minUnit) {
        this.minUnit = minUnit;
    }

    public Long getMaxUnit() {
        return maxUnit;
    }

    public void setMaxUnit(Long maxUnit) {
        this.maxUnit = maxUnit;
    }

    public Long getStepSize() {
        return stepSize;
    }

    public void setStepSize(Long stepSize) {
        this.stepSize = stepSize;
    }

    public Long getAllocationRatio() {
        return allocationRatio;
    }

    public void setAllocationRatio(Long allocationRatio) {
        this.allocationRatio = allocationRatio;
    }
}
