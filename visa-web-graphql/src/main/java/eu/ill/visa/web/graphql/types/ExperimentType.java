package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.Experiment;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("Experiment")
public class ExperimentType {

    private final @NotNull String id;
    private final @NotNull InstrumentType instrument;
    private final @NotNull ProposalType proposal;
    private final String title;
    private final @NotNull Date startDate;
    private final @NotNull Date endDate;

    public ExperimentType(final Experiment experiment) {
        this.id = experiment.getId();
        this.instrument = new InstrumentType(experiment.getInstrument());
        this.proposal = new ProposalType(experiment.getProposal());
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
