package eu.ill.visa.cloud.providers.web.converters;

import eu.ill.visa.cloud.domain.CloudInstanceIdentifier;

import javax.json.JsonObject;

public class InstanceIdentifierConverter {
    
    public static CloudInstanceIdentifier fromJson(final JsonObject json) {
        final String id = json.getString("id");
        final String name = json.getString("name");
        return new CloudInstanceIdentifier(id, name);
    }
}
