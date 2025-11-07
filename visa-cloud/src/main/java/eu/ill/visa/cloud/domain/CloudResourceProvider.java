package eu.ill.visa.cloud.domain;

public class CloudResourceProvider {

    private String uuid;
    private String parentUuid;
    private String name;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHypervisorUuid() {
        return this.parentUuid == null ? this.uuid : this.parentUuid;
    }
}
