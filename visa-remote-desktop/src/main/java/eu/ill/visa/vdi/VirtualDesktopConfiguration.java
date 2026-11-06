package eu.ill.visa.vdi;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Map;
import java.util.Optional;

@ConfigMapping(prefix = "vdi", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface VirtualDesktopConfiguration {

    String protocol();

    int maxSessionInactivityTimeMinutes();

    Optional<String> apiServerAuthToken();

    boolean isMicroservice();

    @WithName("guacd")
    Map<String, String> guacdConfiguration();

    @WithName("webx")
    Map<String, String> webxConfiguration();
}
