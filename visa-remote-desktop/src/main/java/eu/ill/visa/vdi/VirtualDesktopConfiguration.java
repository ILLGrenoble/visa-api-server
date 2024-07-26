package eu.ill.visa.vdi;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Map;

@ConfigMapping(prefix = "vdi", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface VirtualDesktopConfiguration {

    public static String OWNER_DISCONNECTION_POLICY_DISCONNECT_ALL = "DISCONNECT_ALL";
    public static String OWNER_DISCONNECTION_POLICY_LOCK_ROOM = "LOCK_ROOM";

    boolean enabled();

    String ownerDisconnectionPolicy();

    boolean cleanupSessionsOnStartup();

    String protocol();

    @WithName("guacd")
    Map<String, String> guacdConfiguration();
}
