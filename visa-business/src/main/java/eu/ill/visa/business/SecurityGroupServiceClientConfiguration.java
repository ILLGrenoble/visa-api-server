package eu.ill.visa.business;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "business.securityGroupServiceClient")
public interface SecurityGroupServiceClientConfiguration {

    Boolean enabled();
    Optional<String> url();
    Optional<String> authToken();
}

