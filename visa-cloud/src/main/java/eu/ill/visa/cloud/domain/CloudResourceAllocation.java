package eu.ill.visa.cloud.domain;

public class CloudResourceAllocation {

    private String serverComputeId;

    public CloudResourceAllocation() {
    }

    public CloudResourceAllocation(String serverComputeId) {
        this.serverComputeId = serverComputeId;
    }

    public String getServerComputeId() {
        return serverComputeId;
    }

    public void setServerComputeId(String serverComputeId) {
        this.serverComputeId = serverComputeId;
    }
}
