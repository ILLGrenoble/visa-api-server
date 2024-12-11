package eu.ill.visa.core.entity.partial;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class NumberInstancesByFlavour {
    private Long id;
    private String name;
    private Long total ;

    public NumberInstancesByFlavour(final Long id, final String name, final Long total) {
        this.id = id;
        this.name = name;
        this.total = total;
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

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }


}
