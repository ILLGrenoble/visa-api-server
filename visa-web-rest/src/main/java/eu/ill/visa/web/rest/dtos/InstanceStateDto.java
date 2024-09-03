package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.enumerations.InstanceState;

import java.util.Date;
import java.util.List;

public class InstanceStateDto {

    private final InstanceState state;
    private final Date terminationDate;
    private final Date expirationDate;
    private final boolean deleteRequested;
    private final List<String> activeProtocols;

    public InstanceStateDto(final Instance instance) {
        this.state = instance.getState();
        this.terminationDate = instance.getTerminationDate();
        this.expirationDate = instance.getExpirationDate();
        this.deleteRequested = instance.getDeleteRequested();
        this.activeProtocols = instance.getActiveProtocols();
    }

    public InstanceState getState() {
        return state;
    }

    public Date getTerminationDate() {
        return terminationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public boolean isDeleteRequested() {
        return deleteRequested;
    }

    public List<String> getActiveProtocols() {
        return activeProtocols;
    }
}
