package eu.ill.visa.security;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "security", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface SecurityConfiguration {

    String vdiAuthToken();

}
