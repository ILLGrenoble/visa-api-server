package eu.ill.visa.core.domain;

public class NumberInstancesByImage {
    private Long id;
    private String name;
    private String version;
    private Long total ;

    public NumberInstancesByImage() {
    }

    public NumberInstancesByImage(final Long id, final String name, final String version, final Long total) {
        this.id = id;
        this.name = name;
        this.version = version;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }


}
