package eu.ill.visa.cloud;

import java.util.Map;

public class ProviderConfigurationImpl implements ProviderConfiguration {
    private String name;
    private Map<String, String> parameters;

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Map<String, String> parameters() {
        return this.parameters;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
