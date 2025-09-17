package eu.ill.visa.web.graphql.tools;


import eu.ill.visa.cloud.domain.CloudDevice;
import eu.ill.visa.core.entity.enumerations.DeviceType;

public class CloudDeviceTypeConverter {

    public static CloudDevice.Type toCloudDeviceType(final DeviceType deviceType) {
        if (DeviceType.PASSTHROUGH_GPU.equals(deviceType)) {
            return CloudDevice.Type.PASSTHROUGH_GPU;
        } else if (DeviceType.VIRTUAL_GPU.equals(deviceType)) {
            return CloudDevice.Type.VIRTUAL_GPU;
        } else {
            return null;
        }
    }
}
