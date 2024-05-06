package eu.ill.visa.security;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "security", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface SecurityConfiguration {

    @WithName("token")
    TokenConfiguration tokenConfiguration();

}
