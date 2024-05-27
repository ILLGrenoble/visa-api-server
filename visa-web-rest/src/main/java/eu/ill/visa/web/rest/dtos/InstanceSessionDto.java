package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.InstanceSession;

public class InstanceSessionDto {

    private final Long id;
    private final String connectionId;
    private final InstanceDto instance;
    private final boolean current;

    public InstanceSessionDto(final InstanceSession instanceSession) {
        this.id = instanceSession.getId();
        this.connectionId = instanceSession.getConnectionId();
        this.instance = new InstanceDto(instanceSession.getInstance());
        this.current = instanceSession.getCurrent();
    }

    public Long getId() {
        return id;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public InstanceDto getInstance() {
        return instance;
    }

    public boolean isCurrent() {
        return current;
    }
}
