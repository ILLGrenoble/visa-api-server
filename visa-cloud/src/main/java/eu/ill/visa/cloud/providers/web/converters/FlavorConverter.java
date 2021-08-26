package eu.ill.visa.cloud.providers.web.converters;

import eu.ill.visa.cloud.domain.CloudFlavour;

import javax.json.JsonObject;

public class FlavorConverter {

    public static CloudFlavour fromJson(JsonObject json) {
        final CloudFlavour flavor = new CloudFlavour();
        flavor.setId(json.getString("id"));
        flavor.setName(json.getString("name"));
        flavor.setCpus(json.getInt("cpus"));
        flavor.setRam(json.getInt("ram"));
        flavor.setDisk(json.getInt("disk"));
        return flavor;
    }

}
