package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.HypervisorAllocation;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("HypervisorAllocation")
public class HypervisorAllocationType {

    private final @NotNull String serverComputeId;

    public HypervisorAllocationType(final HypervisorAllocation hypervisorAllocation) {
        this.serverComputeId = hypervisorAllocation.getServerComputeId();
    }

    public String getServerComputeId() {
        return serverComputeId;
    }
}
