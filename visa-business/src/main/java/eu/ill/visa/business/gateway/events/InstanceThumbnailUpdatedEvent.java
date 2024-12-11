package eu.ill.visa.business.gateway.events;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record InstanceThumbnailUpdatedEvent(Long instanceId, String instanceUid) {
}
