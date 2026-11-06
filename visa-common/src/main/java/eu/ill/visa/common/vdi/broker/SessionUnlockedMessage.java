package eu.ill.visa.common.vdi.broker;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record SessionUnlockedMessage(String connectionId) {
}
