package eu.ill.visa.web.graphqlxx.types;

import java.util.Date;

public class ExperimentType {
    private String id;
    private InstrumentType instrument;
    private ProposalType proposal;
    private String title;
    private Date startDate;
    private Date endDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InstrumentType getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentType instrument) {
        this.instrument = instrument;
    }

    public ProposalType getProposal() {
        return proposal;
    }

    public void setProposal(ProposalType proposal) {
        this.proposal = proposal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
