package eu.ill.visa.cloud.providers.web.converters;

import eu.ill.visa.cloud.domain.CloudLimit;

import javax.json.JsonObject;

public class LimitConverter {

    public static CloudLimit fromJson(final JsonObject json) {
        final CloudLimit limit = new CloudLimit();
        limit.setMaxTotalRAMSize(json.getInt("maxTotalRamSize"));
        limit.setTotalRAMUsed(json.getInt("totalRamUsed"));
        limit.setTotalInstancesUsed(json.getInt("totalInstancesUsed"));
        limit.setMaxTotalInstances(json.getInt("maxTotalInstances"));
        limit.setMaxTotalCores(json.getInt("maxTotalCores"));
        limit.setTotalCoresUsed(json.getInt("totalCoresUsed"));
        return limit;
    }
}
