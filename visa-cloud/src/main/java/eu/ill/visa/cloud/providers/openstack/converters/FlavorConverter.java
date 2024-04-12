package eu.ill.visa.cloud.providers.openstack.converters;

import eu.ill.visa.cloud.domain.CloudFlavour;

import jakarta.json.JsonObject;


public class FlavorConverter {

    public static CloudFlavour fromJson(JsonObject json) {
        final CloudFlavour flavor = new CloudFlavour();
        flavor.setId(json.getString("id"));
        flavor.setName(json.getString("name"));
        flavor.setCpus(json.getInt("vcpus"));
        flavor.setRam(json.getInt("ram"));
        flavor.setDisk(json.getInt("disk"));
        return flavor;
    }

}
