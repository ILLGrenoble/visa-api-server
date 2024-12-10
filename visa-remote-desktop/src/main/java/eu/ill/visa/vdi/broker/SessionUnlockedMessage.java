package eu.ill.visa.vdi.broker;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record SessionUnlockedMessage(Long sessionId) {
}
