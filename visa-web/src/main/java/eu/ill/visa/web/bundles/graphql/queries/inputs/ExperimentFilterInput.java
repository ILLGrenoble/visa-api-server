package eu.ill.visa.web.bundles.graphql.queries.inputs;

public class ExperimentFilterInput {

    private Long instrumentId;

    public ExperimentFilterInput() {

    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }
}
