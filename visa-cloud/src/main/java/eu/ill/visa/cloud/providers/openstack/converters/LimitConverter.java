package eu.ill.visa.cloud.providers.openstack.converters;

import eu.ill.visa.cloud.domain.CloudLimit;

import javax.json.JsonObject;

public class LimitConverter {

    public static CloudLimit fromJson(final JsonObject json) {
        final JsonObject absolutes = json.getJsonObject("limits").getJsonObject("absolute");
        final CloudLimit limit = new CloudLimit();
        limit.setMaxTotalRAMSize(absolutes.getInt("maxTotalRAMSize"));
        limit.setTotalRAMUsed(absolutes.getInt("totalRAMUsed"));
        limit.setTotalInstancesUsed(absolutes.getInt("totalInstancesUsed"));
        limit.setMaxTotalInstances(absolutes.getInt("maxTotalInstances"));
        limit.setMaxTotalCores(absolutes.getInt("maxTotalCores"));
        limit.setTotalCoresUsed(absolutes.getInt("totalCoresUsed"));
        return limit;
    }
}
