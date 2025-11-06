package eu.ill.visa.cloud.domain;

import java.util.HashMap;
import java.util.Map;

public class CloudHypervisorUsage {

    private final CloudHypervisor hypervisor;
    private final Map<String, Long> usage
        ;

    public CloudHypervisorUsage(CloudHypervisor hypervisor) {
        this.hypervisor = hypervisor;
        this.usage = new HashMap<>();
    }

    public CloudHypervisor getHypervisor() {
        return hypervisor;
    }

    public Map<String, Long> getUsage() {
        return usage;
    }

    public void addResource(CloudResourceUsage resourceUsage) {
        usage.merge(resourceUsage.getResourceClass(), resourceUsage.getUsage(), Long::sum);
    }
}
