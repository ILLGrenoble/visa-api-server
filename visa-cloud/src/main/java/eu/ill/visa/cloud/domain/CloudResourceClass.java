package eu.ill.visa.cloud.domain;


public class CloudResourceClass {

    public final static String VCPU_RESOURCE_CLASS = "VCPU";
    public final static String MEMORY_MB_RESOURCE_CLASS = "MEMORY_MB";
    public final static String DISK_GM_RESOURCE_CLASS = "DISK_GB";

    public static CloudResourceClass VCPUResourceClass() {
        return new CloudResourceClass(VCPU_RESOURCE_CLASS);
    }

    public static CloudResourceClass MemoryMBResourceClass() {
        return new CloudResourceClass(MEMORY_MB_RESOURCE_CLASS);
    }

    public static CloudResourceClass DiskGBResourceClass() {
        return new CloudResourceClass(DISK_GM_RESOURCE_CLASS);
    }

    public static CloudResourceClass CustomResourceClass(final String resourceClass) {
        return new CloudResourceClass(resourceClass);
    }

    private final String name;

    public CloudResourceClass(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
