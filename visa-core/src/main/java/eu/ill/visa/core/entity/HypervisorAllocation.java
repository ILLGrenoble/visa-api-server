package eu.ill.visa.core.entity;


import jakarta.persistence.*;

@Entity
@NamedQueries({
})
@Table(name = "hypervisor_allocation")
public class HypervisorAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "server_compute_id", length = 250, nullable = false)
    private String serverComputeId;


    public HypervisorAllocation() {
    }

    public HypervisorAllocation(String serverComputeId) {
        this.serverComputeId = serverComputeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServerComputeId() {
        return serverComputeId;
    }

    public void setServerComputeId(String serverComputeId) {
        this.serverComputeId = serverComputeId;
    }
}
