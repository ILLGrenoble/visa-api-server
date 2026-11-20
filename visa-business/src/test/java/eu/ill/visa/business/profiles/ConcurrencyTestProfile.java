package eu.ill.visa.business.profiles;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class ConcurrencyTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
        );
    }
}
