package eu.ill.visa.broker.domain.messages;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record EventForRoleMessage(String role, Object event) {
}
