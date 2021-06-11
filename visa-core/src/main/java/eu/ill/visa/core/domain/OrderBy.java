package eu.ill.visa.core.domain;

public class OrderBy {

    private String name;
    private Boolean ascending = true;

    public OrderBy() {

    }

    public OrderBy(String name, Boolean ascending) {
        this.name = name;
        this.ascending = ascending;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAscending() {
        return ascending;
    }
}
