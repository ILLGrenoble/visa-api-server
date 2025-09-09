package eu.ill.visa.cloud.providers.openstack.http.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.domain.CloudInstanceFault;
import eu.ill.visa.cloud.domain.CloudInstanceIdentifier;
import eu.ill.visa.cloud.domain.CloudInstanceState;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Server {
    public String id;
    public String name;
    public String status;
    public @JsonProperty("OS-EXT-STS:task_state") String taskState;
    public @JsonProperty("OS-EXT-STS:vm_state") String vmState;
    public ServerImage image;
    public ServerFlavor flavor;
    public @JsonProperty("addresses") Map<String, List<ServerAddress>> addresses;
    public @JsonProperty("created") Date createdAt;
    public CloudInstanceFault fault;
    public @JsonProperty("security_groups") List<ServerSecurityGroup> securityGroups;


    public static class ServerImage {
        public String id;
    }

    public static class ServerFlavor {
        public String id;
    }

    public static class ServerSecurityGroup {
        public String name;
    }

    public static class ServerAddress {
        public @JsonProperty("addr") String address;
    }

    public CloudInstance toCloudInstance(final String addressProvider) {

        final CloudInstance.Builder builder = CloudInstance.newBuilder();
        builder.id(this.id);
        builder.name(this.name);
        builder.state(convertStatus());
        builder.imageId(this.image.id);
        builder.flavorId(this.flavor.id);
        builder.createdAt(this.createdAt);
        builder.address(address(addressProvider));
        builder.fault(this.fault);
        builder.securityGroups(this.securityGroups == null ? null : this.securityGroups.stream().map(sg -> sg.name).toList());
        return builder.build();
    }

    public CloudInstanceIdentifier toCloudInstanceIdentifier(final String addressProvider) {
        return new CloudInstanceIdentifier(this.id, this.name);
    }

    private CloudInstanceState convertStatus() {
        switch (this.status) {
            case "BUILD":
            case "REBUILD":
                return CloudInstanceState.BUILDING;
            case "ACTIVE":
                if (taskState != null && taskState.equals("powering-off")) {
                    return CloudInstanceState.STOPPING;

                } else {
                    return CloudInstanceState.ACTIVE;
                }
            case "HARD_REBOOT":
            case "REBOOT":
                return CloudInstanceState.REBOOTING;
            case "MIGRATING":
                if (vmState != null && vmState.equals("active")) {
                    return CloudInstanceState.ACTIVE;

                } else {
                    return CloudInstanceState.UNAVAILABLE;
                }
            case "RESCUE":
            case "RESIZE":
            case "REVERT_RESIZE":
            case "VERIFY_SIZE":
                return CloudInstanceState.UNAVAILABLE;
            case "DELETED":
            case "SHELVED":
            case "SHELVED_OFFLOADED":
            case "SOFT_DELETED":
                return CloudInstanceState.DELETED;
            case "PAUSED":
            case "SHUTOFF":
            case "SUSPENDED":
                return CloudInstanceState.STOPPED;
            case "ERROR":
                return CloudInstanceState.ERROR;
            case "UNKNOWN":
            default:
                return CloudInstanceState.UNKNOWN;
        }
    }

    private String address(final String addressProvider) {
        if (this.addresses != null) {
            List<ServerAddress> addresses = this.addresses.get(addressProvider);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.getFirst().address;
            }
        }
        return null;
    }
}
