package eu.ill.visa.cloud.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CloudDevice {

    private static final String PCI_PASSTHROUGH_KEY = "pci_passthrough:alias";
    private static final String RESOURCES_PREFIX = "resources:";
    private static final String VIRTUAL_GPU_PREFIX = "resources:VGPU";

    private String identifier;
    private Type type;

    public CloudDevice() {
    }

    private CloudDevice(String identifier, Type type) {
        this.identifier = identifier;
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CloudDevice) {
            final CloudDevice other = (CloudDevice) object;
            return new EqualsBuilder()
                .append(identifier, other.identifier)
                .append(type, other.type)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(identifier)
            .append(type)
            .toHashCode();
    }

    public enum Type {
        PCI_PASSTHROUGH,
        VIRTUAL_GPU,
    }

    public static CloudDevice from(String key, String value) {
        if (PCI_PASSTHROUGH_KEY.equals(key) && value.indexOf(":") > 0) {
            String alias = value.substring(0, value.indexOf(":"));
            return new CloudDevice(alias, Type.PCI_PASSTHROUGH);

        } else if (key.startsWith(VIRTUAL_GPU_PREFIX)) {
            String alias = key.substring(RESOURCES_PREFIX.length());
            return new CloudDevice(alias, Type.VIRTUAL_GPU);

        } else {
            return null;
        }
    }

}
