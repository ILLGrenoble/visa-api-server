package eu.ill.visa.core.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Flavour extends Timestampable {

    private Long id;

    private String name;

    private Integer memory;

    private Float cpu;

    private String computeId;

    private Boolean deleted = false;

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

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Flavour flavour = (Flavour) o;

        return new EqualsBuilder()
            .append(id, flavour.id)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }


    public static final class Builder {
        private Long id;
        private String name;
        private Integer memory;
        private Float cpu;
        private String computeId;

        public Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder memory(Integer memory) {
            this.memory = memory;
            return this;
        }

        public Builder cpu(Float cpu) {
            this.cpu = cpu;
            return this;
        }

        public Builder computeId(String computeId) {
            this.computeId = computeId;
            return this;
        }

        public Flavour build() {
            Flavour flavour = new Flavour();
            flavour.setId(id);
            flavour.setName(name);
            flavour.setMemory(memory);
            flavour.setCpu(cpu);
            flavour.setComputeId(computeId);
            return flavour;
        }
    }
}
