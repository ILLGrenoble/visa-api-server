package eu.ill.visa.cloud;

import java.util.Map;

public interface ProviderConfiguration {
    String name();
    Map<String, String> parameters();
}
