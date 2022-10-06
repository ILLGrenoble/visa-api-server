package eu.ill.visa.core.domain;

public class NumberInstancesByCloudClient {
    private Long id;
    private String name;
    private Long total ;

    public NumberInstancesByCloudClient() {
    }

    public NumberInstancesByCloudClient(final Long id, final String name, final Long total) {
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
