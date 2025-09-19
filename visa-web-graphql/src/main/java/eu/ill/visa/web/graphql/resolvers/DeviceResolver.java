package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.cloud.domain.CloudDevice;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.CloudDeviceType;
import eu.ill.visa.web.graphql.types.DevicePoolType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;

import static eu.ill.visa.business.tools.CloudDeviceTypeConverter.toCloudDeviceType;

@RegisterForReflection
@GraphQLApi
public class DeviceResolver {

    private final CloudClientService cloudClientService;

    public DeviceResolver(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
    }

    public List<CloudClientType> cloudClient(@Source List<DevicePoolType> devicePools) {
        List<CloudClient> cloudClients = this.cloudClientService.getAll();
        return devicePools.stream().map(devicePool -> {
            return cloudClients.stream().filter(cloudClient -> {
                return cloudClient.getId() == -1 ? devicePool.getCloudId() == null : cloudClient.getId().equals(devicePool.getCloudId());
            }).findFirst().orElse(null);
        }).map(cloudClient -> cloudClient == null ? null : new CloudClientType(cloudClient)).toList();
    }

    public CloudDeviceType cloudDevice(@Source DevicePoolType devicePool) {
        try {
            CloudClient cloudClient = this.cloudClientService.getAll().stream().filter(aCloudClient -> {
                return aCloudClient.getId() == -1 ? devicePool.getCloudId() == null : aCloudClient.getId().equals(devicePool.getCloudId());
            }).findFirst().orElse(null);

            if (cloudClient != null) {
                final CloudDevice.Type cloudDeviceType = toCloudDeviceType(devicePool.getDeviceType());
                CloudDevice cloudDevice = cloudClient.device(devicePool.getComputeIdentifier(), cloudDeviceType);
                if (cloudDevice != null) {
                    return new CloudDeviceType(cloudDevice);
                }
            }
        } catch (CloudException ignored) {
        }
        return null;
    }
}
