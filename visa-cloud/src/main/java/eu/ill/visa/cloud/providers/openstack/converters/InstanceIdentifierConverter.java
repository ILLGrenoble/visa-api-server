package eu.ill.visa.cloud.providers.openstack.converters;

import eu.ill.visa.cloud.domain.CloudInstanceIdentifier;

import javax.json.JsonObject;

public class InstanceIdentifierConverter {

    private static final String ID                = "id";
    private static final String NAME              = "name";

    public static CloudInstanceIdentifier fromJson(final JsonObject json) {

        final String id = json.getString(ID);
        final String name = json.getString(NAME);

        return new CloudInstanceIdentifier(id, name);
    }

}
