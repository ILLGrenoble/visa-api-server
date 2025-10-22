package eu.ill.visa.cloud.domain;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CloudDeviceAllocation {
    String flavourId;
    CloudDevice device;
    Integer unitCount;

    public CloudDeviceAllocation() {
    }

    public CloudDeviceAllocation(String flavourId, CloudDevice device, Integer unitCount) {
        this.flavourId = flavourId;
        this.device = device;
        this.unitCount = unitCount;
    }

    public String getFlavourId() {
        return flavourId;
    }

    public void setFlavourId(String flavourId) {
        this.flavourId = flavourId;
    }

    public CloudDevice getDevice() {
        return device;
    }

    public void setDevice(CloudDevice device) {
        this.device = device;
    }

    public Integer getUnitCount() {
        return unitCount;
    }

    public void setUnitCount(Integer unitCount) {
        this.unitCount = unitCount;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CloudDeviceAllocation) {
            final CloudDeviceAllocation other = (CloudDeviceAllocation) object;
            return new EqualsBuilder()
                .append(flavourId, other.flavourId)
                .append(device, other.device)
                .append(unitCount, other.unitCount)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(flavourId)
            .append(device)
            .append(unitCount)
            .toHashCode();
    }

    public static CloudDeviceAllocation from(String flavourId, String key, String value) {
        try {
            if (CloudDevice.PCI_PASSTHROUGH_KEY.equals(key) && value.indexOf(":") > 0 && value.indexOf(":") < value.length() - 1) {
                String alias = value.substring(0, value.indexOf(":"));
                Integer unitCount = Integer.parseInt(value.substring(value.indexOf(":") + 1));
                CloudDevice cloudDevice = new CloudDevice(alias, CloudDevice.Type.PCI_PASSTHROUGH);
                return new CloudDeviceAllocation(flavourId, cloudDevice, unitCount);

            } else if (key.startsWith(CloudDevice.VIRTUAL_GPU_PREFIX)) {
                String alias = key.substring(CloudDevice.RESOURCES_PREFIX.length());
                Integer unitCount = Integer.parseInt(value);
                CloudDevice cloudDevice = new CloudDevice(alias, CloudDevice.Type.VIRTUAL_GPU);
                return new CloudDeviceAllocation(flavourId, cloudDevice, unitCount);

            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }
}
