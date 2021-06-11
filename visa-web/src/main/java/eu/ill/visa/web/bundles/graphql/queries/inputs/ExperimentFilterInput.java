package eu.ill.visa.web.bundles.graphql.queries.inputs;

public class ExperimentFilterInput {
    private Long cycleId;

    private Long instrumentId;

    public ExperimentFilterInput() {

    }

    public Long getCycleId() {
        return cycleId;
    }

    public void setCycleId(Long cycleId) {
        this.cycleId = cycleId;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }
}
