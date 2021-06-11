package eu.ill.visa.cloud.providers.openstack.converters;

import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

public class AuthenticationConverter {

    public static String toJson(final OpenStackProviderConfiguration configuration) {
        JsonObjectBuilder auth     = Json.createObjectBuilder();
        JsonObjectBuilder identity = Json.createObjectBuilder();
        JsonArrayBuilder  methods  = Json.createArrayBuilder();
        methods.add("application_credential");
        identity.add("methods", methods);

        JsonObjectBuilder applicationCredential = Json.createObjectBuilder();
        applicationCredential.add("id", configuration.getApplicationId());
        applicationCredential.add("secret", configuration.getApplicationSecret());
        identity.add("application_credential", applicationCredential);

        auth.add("identity", identity);

        return Json.createObjectBuilder().add("auth", auth).build().toString();
    }
}
