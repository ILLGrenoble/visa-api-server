package eu.ill.visa.vdi;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Map;
import java.util.Optional;

@ConfigMapping(prefix = "vdi", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface VirtualDesktopConfiguration {

    public static String OWNER_DISCONNECTION_POLICY_DISCONNECT_ALL = "DISCONNECT_ALL";
    public static String OWNER_DISCONNECTION_POLICY_LOCK_ROOM = "LOCK_ROOM";

    boolean enabled();

    Integer port();

    String host();

    String corsOrigin();

    boolean redisEnabled();

    Optional<String> redisURL();

    Optional<String> redisPassword();

    Integer redisDatabase();

    String ownerDisconnectionPolicy();

    boolean cleanupSessionsOnStartup();

    int pingTimeout();

    int pingInterval();

    String protocol();

    @WithName("guacd")
    Map<String, String> guacdConfiguration();
}
