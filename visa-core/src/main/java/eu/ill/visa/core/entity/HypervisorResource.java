package eu.ill.visa.core.entity;


import jakarta.persistence.*;

@Entity
@NamedQueries({
})
@Table(name = "hypervisor_resource")
public class HypervisorResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "resource_class", length = 250, nullable = false)
    private String resourceClass;

    @Column(name = "total", nullable = false)
    private Long total;

    @Column(name = "usage", nullable = true)
    private Long usage;

    public HypervisorResource() {
    }

    public HypervisorResource(String resourceClass, Long total) {
        this.resourceClass = resourceClass;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getUsage() {
        return usage;
    }

    public void setUsage(Long usage) {
        this.usage = usage;
    }
}
