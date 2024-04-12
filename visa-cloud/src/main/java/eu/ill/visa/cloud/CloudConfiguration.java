package eu.ill.visa.cloud;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "cloud")
public interface CloudConfiguration {

    String providerType();

    String providerName();

    List<ProviderConfiguration> providers();

    String serverNamePrefix();

}
