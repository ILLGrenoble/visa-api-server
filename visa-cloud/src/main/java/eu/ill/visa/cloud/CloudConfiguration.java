package eu.ill.visa.cloud;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "cloud", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface CloudConfiguration {

    Boolean defaultProviderEnabled();

    String providerType();

    String providerName();

    Long restClientReadTimeoutMs();

    Long restClientConnectTimeoutMs();

    List<ProviderConfiguration> providers();

    String serverNamePrefix();

}
