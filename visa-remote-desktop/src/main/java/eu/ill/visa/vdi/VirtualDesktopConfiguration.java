package eu.ill.visa.vdi;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ill.visa.vdi.configuration.SignatureConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class VirtualDesktopConfiguration {

    public static String OWNER_DISCONNECTION_POLICY_DISCONNECT_ALL = "DISCONNECT_ALL";
    public static String OWNER_DISCONNECTION_POLICY_LOCK_ROOM = "LOCK_ROOM";


    @NotNull
    private Integer port = 8087;

    @NotNull
    private String host = "localhost";

    @NotNull
    private String corsOrigin;

    @NotNull
    private boolean redisEnabled;

    @NotNull
    private String redisURL;

    @NotNull
    private String redisPassword;

    @NotNull
    private Integer redisDatabase;

    @NotNull
    private String ownerDisconnectionPolicy = OWNER_DISCONNECTION_POLICY_DISCONNECT_ALL;

    @NotNull
    private boolean cleanupSessionsOnStartup = false;

    @NotNull
    private boolean enabled = true;

    @NotNull
    private int pingTimeout;

    @NotNull
    private int pingInterval;

    @NotNull
    @Valid
    private SignatureConfiguration signatureConfiguration;

    @NotNull
    @Valid
    private Map<String, String> guacdConfiguration = new HashMap<>();

    @NotNull
    @Valid
    private String protocol;

    public VirtualDesktopConfiguration() {

    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getCorsOrigin() {
        return corsOrigin;
    }

    public void setCorsOrigin(String corsOrigin) {
        this.corsOrigin = corsOrigin;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public void setRedisEnabled(boolean redisEnabled) {
        this.redisEnabled = redisEnabled;
    }

    public String getRedisURL() {
        return redisURL;
    }

    public void setRedisURL(String redisURL) {
        this.redisURL = redisURL;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public Integer getRedisDatabase() {
        return redisDatabase;
    }

    public void setRedisDatabase(Integer redisDatabase) {
        this.redisDatabase = redisDatabase;
    }

    public String getOwnerDisconnectionPolicy() {
        return ownerDisconnectionPolicy;
    }

    public void setOwnerDisconnectionPolicy(String ownerDisconnectionPolicy) {
        this.ownerDisconnectionPolicy = ownerDisconnectionPolicy;
    }

    public boolean isCleanupSessionsOnStartup() {
        return cleanupSessionsOnStartup;
    }

    public void setCleanupSessionsOnStartup(boolean cleanupSessionsOnStartup) {
        this.cleanupSessionsOnStartup = cleanupSessionsOnStartup;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty("signature")
    public SignatureConfiguration getSignatureConfiguration() {
        return signatureConfiguration;
    }

    public int getPingTimeout() {
        return pingTimeout;
    }

    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    @JsonProperty("guacd")
    public Map<String, String> getGuacdConfiguration() {
        return guacdConfiguration;
    }

    public void setGuacdConfiguration(Map<String, String> guacdConfiguration) {
        this.guacdConfiguration = guacdConfiguration;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
