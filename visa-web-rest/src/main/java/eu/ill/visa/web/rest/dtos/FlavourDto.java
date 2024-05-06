package eu.ill.visa.web.rest.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FlavourDto {

    @NotNull
    private Long id;

    @NotNull
    @Size(min = 1, max = 250)
    private String name;

    @NotNull
    @Min(1)
    private Integer memory;

    @NotNull
    private Float cpu;

    public FlavourDto() {
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

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public Float getCpu() {
        return cpu;
    }

    public void setCpu(Float cpu) {
        this.cpu = cpu;
    }
}
