package eu.ill.visa.business.tools;


import eu.ill.visa.cloud.domain.CloudDevice;
import eu.ill.visa.core.entity.enumerations.DeviceType;

public class CloudDeviceTypeConverter {

    public static CloudDevice.Type toCloudDeviceType(final DeviceType deviceType) {
        if (DeviceType.PCI_PASSTHROUGH.equals(deviceType)) {
            return CloudDevice.Type.PCI_PASSTHROUGH;
        } else if (DeviceType.VIRTUAL_GPU.equals(deviceType)) {
            return CloudDevice.Type.VIRTUAL_GPU;
        } else {
            return null;
        }
    }

    public static DeviceType fromCloudDeviceType(final CloudDevice.Type cloudDeviceType) {
        if (CloudDevice.Type.PCI_PASSTHROUGH.equals(cloudDeviceType)) {
            return DeviceType.PCI_PASSTHROUGH;
        } else if (CloudDevice.Type.VIRTUAL_GPU.equals(cloudDeviceType)) {
            return DeviceType.VIRTUAL_GPU;
        } else {
            return null;
        }
    }
}
