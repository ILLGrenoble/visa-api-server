package eu.ill.visa.security;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;

@ConfigMapping(prefix = "security", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface SecurityConfiguration {

    Optional<String> vdiAuthToken();

}
