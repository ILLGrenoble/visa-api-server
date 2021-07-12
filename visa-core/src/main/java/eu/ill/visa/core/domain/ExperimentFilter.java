package eu.ill.visa.core.domain;

import java.util.Date;

public class ExperimentFilter {

    private Cycle cycle;
    private Date startDate;
    private Date endDate;
    private Instrument instrument;

    public ExperimentFilter() {

    }

    public ExperimentFilter(final Cycle cycle, final Instrument instrument) {
        this.cycle = cycle;
        this.instrument = instrument;
    }

    public ExperimentFilter(Cycle cycle) {
        this.cycle = cycle;
    }

    public ExperimentFilter(Date startDate, Date endDate, Instrument instrument) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.instrument = instrument;
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

    public Cycle getCycle() {
        return cycle;
    }

    public Instrument getInstrument() {
        return instrument;
    }
}