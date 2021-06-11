package eu.ill.visa.cloud.domain;

public class CloudLimit {

    private Integer maxTotalRAMSize;
    private Integer totalRAMUsed;
    private Integer totalInstancesUsed;
    private Integer maxTotalInstances;
    private Integer maxTotalCores;
    private Integer totalCoresUsed;


    public Integer getMaxTotalRAMSize() {
        return maxTotalRAMSize;
    }

    public void setMaxTotalRAMSize(Integer maxTotalRAMSize) {
        this.maxTotalRAMSize = maxTotalRAMSize;
    }

    public Integer getTotalRAMUsed() {
        return totalRAMUsed;
    }

    public void setTotalRAMUsed(Integer totalRAMUsed) {
        this.totalRAMUsed = totalRAMUsed;
    }

    public void setTotalInstancesUsed(Integer totalInstancesUsed) {
        this.totalInstancesUsed = totalInstancesUsed;
    }

    public Integer getTotalInstancesUsed() {
        return totalInstancesUsed;
    }

    public Integer getMaxTotalInstances() {
        return maxTotalInstances;
    }

    public void setMaxTotalInstances(Integer maxTotalInstances) {
        this.maxTotalInstances = maxTotalInstances;
    }

    public Integer getMaxTotalCores() {
        return maxTotalCores;
    }

    public void setMaxTotalCores(Integer maxTotalCores) {
        this.maxTotalCores = maxTotalCores;
    }

    public Integer getTotalCoresUsed() {
        return totalCoresUsed;
    }

    public void setTotalCoresUsed(Integer totalCoresUsed) {
        this.totalCoresUsed = totalCoresUsed;
    }
}
