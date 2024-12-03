package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.InstanceSession;

public class InstanceSessionDto {

    private final Long id;
    private final String connectionId;
    private final Long instanceId;
    private final boolean current;

    public InstanceSessionDto(final InstanceSession instanceSession) {
        this.id = instanceSession.getId();
        this.connectionId = instanceSession.getConnectionId();
        this.instanceId = instanceSession.getInstanceId();
        this.current = instanceSession.getCurrent();
    }

    public Long getId() {
        return id;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public boolean isCurrent() {
        return current;
    }
}
