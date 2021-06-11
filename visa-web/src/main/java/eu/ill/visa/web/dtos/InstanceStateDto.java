package eu.ill.visa.web.dtos;

import eu.ill.visa.core.domain.enumerations.InstanceState;

import java.util.Date;

public class InstanceStateDto {

    private InstanceState state;
    private Date terminationDate;
    private Date expirationDate;
    private boolean deleteRequested;

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
}
