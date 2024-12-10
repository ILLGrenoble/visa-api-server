package eu.ill.visa.broker.domain.models;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ClientEventCarrier(String type, Object data) {
}
