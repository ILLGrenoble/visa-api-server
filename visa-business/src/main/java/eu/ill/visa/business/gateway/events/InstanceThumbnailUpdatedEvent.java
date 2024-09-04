package eu.ill.visa.business.gateway.events;

import eu.ill.visa.core.entity.Instance;

public record InstanceThumbnailUpdatedEvent(Long instanceId, String instanceUid) {

    public InstanceThumbnailUpdatedEvent(final Instance instance) {
        this(instance.getId(), instance.getUid());
    }
}
