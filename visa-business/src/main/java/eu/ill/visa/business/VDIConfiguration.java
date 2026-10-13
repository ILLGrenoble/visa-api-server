package eu.ill.visa.business;

public interface VDIConfiguration {

    String OWNER_DISCONNECTION_POLICY_DISCONNECT_ALL = "DISCONNECT_ALL";
    String OWNER_DISCONNECTION_POLICY_LOCK_ROOM = "LOCK_ROOM";

    String ownerDisconnectionPolicy();

    boolean cleanupSessionsOnStartup();

}
