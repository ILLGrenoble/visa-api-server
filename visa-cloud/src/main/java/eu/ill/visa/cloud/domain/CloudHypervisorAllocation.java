package eu.ill.visa.cloud.domain;

import java.util.ArrayList;
import java.util.List;

public class CloudHypervisorAllocation {

    private final CloudHypervisor hypervisor;
    private final List<CloudResourceAllocation> allocations;

    public CloudHypervisorAllocation(CloudHypervisor hypervisor) {
        this.hypervisor = hypervisor;
        this.allocations = new ArrayList<>();
    }

    public CloudHypervisor getHypervisor() {
        return hypervisor;
    }

    public List<CloudResourceAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<CloudResourceAllocation> allocations) {
        this.allocations.clear();
        this.allocations.addAll(allocations);
    }

    public void addAllocation(final CloudResourceAllocation allocation) {
        this.allocations.add(allocation);
    }
}
