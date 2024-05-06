package eu.ill.visa.web.rest.dtos;

public class InstrumentDto {
    private Long id;
    private String name;

    public InstrumentDto() {
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
