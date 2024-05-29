package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.Experiment;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("Experiment")
public class ExperimentType {

    private final String id;
    private final InstrumentType instrument;
    private final ProposalType proposal;
    private final String title;
    private final Date startDate;
    private final Date endDate;

    public ExperimentType(final Experiment experiment) {
        this.id = experiment.getId();
        this.instrument = experiment.getInstrument() == null ? null : new InstrumentType(experiment.getInstrument());
        this.proposal = experiment.getProposal() == null ? null : new ProposalType(experiment.getProposal());
        this.title = experiment.getTitle();
        this.startDate = experiment.getStartDate();
        this.endDate = experiment.getEndDate();
    }

    public String getId() {
        return id;
    }

    public InstrumentType getInstrument() {
        return instrument;
    }

    public ProposalType getProposal() {
        return proposal;
    }

    public String getTitle() {
        return title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}
