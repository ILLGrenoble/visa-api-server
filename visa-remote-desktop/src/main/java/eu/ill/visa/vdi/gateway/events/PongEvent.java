package eu.ill.visa.vdi.gateway.events;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record PongEvent(String clientId, long time) {
}
