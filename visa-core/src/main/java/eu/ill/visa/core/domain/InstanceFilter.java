package eu.ill.visa.core.domain;

import eu.ill.visa.core.domain.enumerations.InstanceState;

public class InstanceFilter {

    private InstanceState state;
    private Long id;
    private String owner;
    private String name;
    private Long instrumentId;

    public InstanceFilter() {
    }

    public InstanceFilter(Long id, String owner, String name, Long instrumentId) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.instrumentId = instrumentId;
    }

    public InstanceState getState() {
        return state;
    }

    public InstanceFilter state(InstanceState state) {
        this.state = state;
        return this;
    }

    public Long getId() {
        return id;
    }

    public InstanceFilter id(Long id) {
        this.id = id;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public InstanceFilter owner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getName() {
        return name;
    }

    public InstanceFilter name(String name) {
        this.name = name;
        return this;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public InstanceFilter instrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
        return this;
    }
}
