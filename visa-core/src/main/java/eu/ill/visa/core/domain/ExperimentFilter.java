package eu.ill.visa.core.domain;

import eu.ill.visa.core.entity.Instrument;

import java.util.Date;
import java.util.Set;

public class ExperimentFilter {

    private Date startDate;
    private Date endDate;
    private Instrument instrument;
    private Set<String> proposalIdentifiers;
    private Set<String> dois;
    private Boolean includeOpenData = Boolean.FALSE;

    public ExperimentFilter() {

    }

    public ExperimentFilter(Date startDate, Date endDate, Instrument instrument) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.instrument = instrument;
    }

    public ExperimentFilter(Date startDate, Date endDate, Instrument instrument, Set<String> proposalIdentifiers, Set<String> dois, Boolean includeOpenData) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.instrument = instrument;
        this.proposalIdentifiers = proposalIdentifiers;
        this.dois = dois;
        this.includeOpenData = includeOpenData;
    }

    public ExperimentFilter(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public ExperimentFilter(Instrument instrument) {
        this.instrument = instrument;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public Set<String> getProposalIdentifiers() {
        return proposalIdentifiers;
    }

    public Set<String> getDois() {
        return dois;
    }

    public Boolean getIncludeOpenData() {
        return includeOpenData;
    }
}
