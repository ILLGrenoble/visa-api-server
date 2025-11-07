package eu.ill.visa.cloud.domain;

public class CloudResourceUsage {

    private CloudResourceClass resourceClass;
    private Long usage;

    public CloudResourceUsage() {
    }

    public CloudResourceUsage(CloudResourceClass resourceClass, Long resourceUsage) {
        this.resourceClass = resourceClass;
        this.usage = resourceUsage;
    }

    public CloudResourceClass getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(CloudResourceClass resourceClass) {
        this.resourceClass = resourceClass;
    }

    public Long getUsage() {
        return usage;
    }

    public void setUsage(Long usage) {
        this.usage = usage;
    }
}
