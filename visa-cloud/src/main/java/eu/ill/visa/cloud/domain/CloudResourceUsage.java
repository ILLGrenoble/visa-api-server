package eu.ill.visa.cloud.domain;

public class CloudResourceUsage {

    private String resourceClass;
    private Long usage;

    public CloudResourceUsage() {
    }

    public CloudResourceUsage(String resourceClass, Long resourceUsage) {
        this.resourceClass = resourceClass;
        this.usage = resourceUsage;
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }

    public Long getUsage() {
        return usage;
    }

    public void setUsage(Long usage) {
        this.usage = usage;
    }
}
