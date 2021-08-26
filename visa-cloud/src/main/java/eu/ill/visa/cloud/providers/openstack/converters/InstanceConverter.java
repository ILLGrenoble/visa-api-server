package eu.ill.visa.cloud.providers.openstack.converters;

import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.domain.CloudInstanceFault;
import eu.ill.visa.cloud.domain.CloudInstanceState;
import org.joda.time.DateTime;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InstanceConverter {

    private static final String ID                = "id";
    private static final String NAME              = "name";
    private static final String STATUS            = "status";
    private static final String TASK_STATE        = "OS-EXT-STS:task_state";
    private static final String IMAGE             = "image";
    private static final String FLAVOR            = "flavor";
    private static final String ADDRESSES         = "addresses";
    private static final String ADDRESSES_ADDRESS = "addr";
    private static final String CREATED           = "created";

    public static CloudInstance fromJson(final JsonObject json, final String addressProvider) {

        final String id = json.getString(ID);
        final String name = json.getString(NAME);
        final DateTime createdAt = new DateTime(json.getString(CREATED));
        final JsonObject image = json.getJsonObject(IMAGE);
        final JsonObject flavor = json.getJsonObject(FLAVOR);
        final String taskState = getTaskState(json.get(TASK_STATE));
        final CloudInstanceState state = convertStatus(json.getString(STATUS), taskState);

        final CloudInstance.Builder builder = CloudInstance.newBuilder();

        final String address = address(json, addressProvider);
        final CloudInstanceFault fault = fault(json);
        final List<String> securityGroups = securityGroups(json);
        builder.address(address);
        builder.id(id);
        builder.name(name);
        builder.state(state);
        builder.imageId(image.getString(ID));
        builder.flavorId(flavor.getString(ID));
        builder.createdAt(createdAt);
        builder.fault(fault);
        builder.securityGroups(securityGroups);
        return builder.build();
    }

    private static String getTaskState(JsonValue taskStateValue) {
        if (taskStateValue.getValueType().equals(JsonValue.ValueType.STRING)) {
            return ((JsonString) taskStateValue).getString();

        } else {
            return "";
        }
    }

    private static CloudInstanceFault fault(final JsonObject json) {
        final JsonObject fault = json.getJsonObject("fault");
        if (fault == null) {
            return null;
        }
        return new CloudInstanceFault(
            fault.getString("message"),
            fault.getInt("code"),
            fault.containsKey("details") ? fault.getString("details") : null,
            fault.getString("created"));

    }

    private static String address(final JsonObject json, String addressProvider) {
        final JsonObject addresses = json.getJsonObject(ADDRESSES);
        if (addresses == null || addresses.getJsonArray(addressProvider) == null) {
            return null;
        }
        return addresses
            .getJsonArray(addressProvider)
            .get(0)
            .asJsonObject()
            .getString(ADDRESSES_ADDRESS);
    }

    private static List<String> securityGroups(final JsonObject json) {
        final JsonArray securityGroups = json.getJsonArray("security_groups");
        if (securityGroups != null) {
            return IntStream.range(0, securityGroups.size())
                .mapToObj(securityGroups::getJsonObject)
                .map(securityGroup -> securityGroup.getString("name")).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private static CloudInstanceState convertStatus(String openStackStatus, String openStackTaskState) {
        switch (openStackStatus) {
            case "BUILD":
            case "REBUILD":
                return CloudInstanceState.BUILDING;
            case "ACTIVE":
                if (openStackTaskState.equals("powering-off")) {
                    return CloudInstanceState.STOPPING;

                } else {
                    return CloudInstanceState.ACTIVE;
                }
            case "HARD_REBOOT":
            case "REBOOT":
                return CloudInstanceState.REBOOTING;
            case "MIGRATING":
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
}
