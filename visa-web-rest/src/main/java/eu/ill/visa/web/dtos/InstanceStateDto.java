package eu.ill.visa.web.dtos;

import eu.ill.visa.core.domain.enumerations.InstanceState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InstanceStateDto {

    private InstanceState state;
    private Date terminationDate;
    private Date expirationDate;
    private boolean deleteRequested;
    private List<String> activeProtocols = new ArrayList<>();

    public InstanceState getState() {
        return state;
    }

    public void setState(InstanceState state) {
        this.state = state;
    }

    public Date getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(Date terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isDeleteRequested() {
        return deleteRequested;
    }

    public void setDeleteRequested(boolean deleteRequested) {
        this.deleteRequested = deleteRequested;
    }

    public List<String> getActiveProtocols() {
        return activeProtocols;
    }

    public void setActiveProtocols(List<String> activeProtocols) {
        this.activeProtocols = activeProtocols;
    }
}
