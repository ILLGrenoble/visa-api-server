package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.DevicePool;

public class DevicePoolDto {

    private final Long id;
    private final String name;
    private final String description;

    public DevicePoolDto(final DevicePool devicePool) {
        this.id = devicePool.getId();
        this.name = devicePool.getName();
        this.description = devicePool.getDescription();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
