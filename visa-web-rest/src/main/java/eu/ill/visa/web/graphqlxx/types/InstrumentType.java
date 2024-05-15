package eu.ill.visa.web.graphqlxx.types;

public class InstrumentType {
    private Long id;
    private String name;

    public InstrumentType() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
