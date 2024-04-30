package eu.ill.visa.business;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "business.securityGroupServiceClient", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface SecurityGroupServiceClientConfiguration {

    Boolean enabled();
    Optional<String> url();
    Optional<String> authToken();
}

