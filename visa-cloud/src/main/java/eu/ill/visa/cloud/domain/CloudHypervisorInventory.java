package eu.ill.visa.cloud.domain;

import java.util.HashMap;
import java.util.Map;

public class CloudHypervisorInventory {

    private final CloudHypervisor hypervisor;
    private final Map<String, Long> inventory;

    public CloudHypervisorInventory(CloudHypervisor hypervisor) {
        this.hypervisor = hypervisor;
        this.inventory = new HashMap<>();
    }

    public CloudHypervisor getHypervisor() {
        return hypervisor;
    }

    public Map<String, Long> getInventory() {
        return inventory;
    }

    public void addResource(CloudResourceInventory resourceInventory) {
        Long total = resourceInventory.getTotal() * resourceInventory.getAllocationRatio();
        inventory.merge(resourceInventory.getResourceClass().getName(), total, Long::sum);
    }
}
