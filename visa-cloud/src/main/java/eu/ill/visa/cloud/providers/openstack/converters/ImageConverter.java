package eu.ill.visa.cloud.providers.openstack.converters;

import eu.ill.visa.cloud.domain.CloudImage;

import jakarta.json.JsonObject;

public class ImageConverter {

    public static CloudImage fromJson(final JsonObject json) {
        final CloudImage image = new CloudImage();
        image.setId(json.getString("id"));
        image.setName(json.getString("name"));
        image.setSize(json.getJsonNumber("size").longValue());
        image.setCreatedAt(json.getString("created_at"));
        return image;
    }
}
