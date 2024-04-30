package eu.ill.visa.business.profiles;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class ConcurrencyTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "quarkus.keycloak.devservices.enabled", "false",
            "quarkus.oidc.enabled", "false",
            "quarkus.oidc.auth-server-url", "http://test.ill.fr"
        );
    }
}
