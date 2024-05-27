package eu.ill.visa.web.rest.dtos;


import com.fasterxml.jackson.annotation.JsonFormat;
import eu.ill.visa.core.entity.Experiment;

import java.util.Date;

public class ExperimentDto {
    private final String id;
    private final InstrumentDto instrument;
    private final ProposalDto proposal;
    private final String title;
    private final String url;
    private final String doi;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final Date startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final Date endDate;

    public ExperimentDto(final Experiment experiment) {
        this.id = experiment.getId();
        this.instrument = new InstrumentDto(experiment.getInstrument());
        this.proposal = new ProposalDto(experiment.getProposal());
        this.title = experiment.getTitle();
        this.url = experiment.getUrl();
        this.doi = experiment.getDoi();
        this.startDate = experiment.getStartDate();
        this.endDate = experiment.getEndDate();
    }

    public String getId() {
        return id;
    }

    public InstrumentDto getInstrument() {
        return instrument;
    }

    public ProposalDto getProposal() {
        return proposal;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDoi() {
        return doi;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
