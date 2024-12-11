package eu.ill.visa.vdi.broker;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record SessionClosedMessage(Long sessionId) {
}
