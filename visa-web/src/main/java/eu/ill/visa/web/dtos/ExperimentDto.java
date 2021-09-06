package eu.ill.visa.web.dtos;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ExperimentDto {
    private String        id;
    private InstrumentDto instrument;
    private ProposalDto   proposal;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date endDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InstrumentDto getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentDto instrument) {
        this.instrument = instrument;
    }

    public ProposalDto getProposal() {
        return proposal;
    }

    public void setProposal(ProposalDto proposal) {
        this.proposal = proposal;
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
