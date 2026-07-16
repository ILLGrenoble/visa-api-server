package eu.ill.visa.vdi.gateway.events;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PingEvent(String clientId, long time) {
    public PingEvent(String clientId) {
        this(clientId, System.currentTimeMillis());
    }
}
