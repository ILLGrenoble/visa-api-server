package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.Hypervisor;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.List;

@Type("Hypervisor")
public class HypervisorType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String computeId;
    private final @NotNull String hostname;
    private final String state;
    private final String status;
    private final Long cloudId;
    private final @NotNull List<HypervisorResourceType> resources;

    public HypervisorType(final Hypervisor hypervisor) {
        this.id = hypervisor.getId();
        this.computeId = hypervisor.getComputeId();
        this.hostname = hypervisor.getHostname();
        this.state = hypervisor.getState();
        this.status = hypervisor.getStatus();
        this.cloudId = hypervisor.getCloudId();
        this.resources = hypervisor.getResources().stream().map(HypervisorResourceType::new).toList();
    }

    public Long getId() {
        return id;
    }

    public String getComputeId() {
        return computeId;
    }

    public String getHostname() {
        return hostname;
    }

    public String getState() {
        return state;
    }

    public String getStatus() {
        return status;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public List<HypervisorResourceType> getResources() {
        return resources;
    }
}
