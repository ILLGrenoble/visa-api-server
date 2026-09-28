package eu.ill.visa.vdi;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Map;

@ConfigMapping(prefix = "vdi", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface VirtualDesktopConfiguration {

    String OWNER_DISCONNECTION_POLICY_DISCONNECT_ALL = "DISCONNECT_ALL";
    String OWNER_DISCONNECTION_POLICY_LOCK_ROOM = "LOCK_ROOM";

    boolean enabled();

    String ownerDisconnectionPolicy();

    boolean cleanupSessionsOnStartup();

    String protocol();

    int numberOfWsThreads();

    int maxSessionInactivityTimeMinutes();

    @WithName("guacd")
    Map<String, String> guacdConfiguration();

    @WithName("webx")
    Map<String, String> webxConfiguration();
}
