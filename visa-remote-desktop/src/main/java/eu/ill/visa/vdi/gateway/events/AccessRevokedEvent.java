package eu.ill.visa.vdi.gateway.events;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AccessRevokedEvent(String userId) {
}
