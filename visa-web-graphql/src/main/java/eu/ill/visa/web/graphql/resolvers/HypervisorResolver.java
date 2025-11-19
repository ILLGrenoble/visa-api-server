package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.web.graphql.types.HypervisorAllocationType;
import eu.ill.visa.web.graphql.types.InstanceType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;

@RegisterForReflection
@GraphQLApi
public class HypervisorResolver {

    private final InstanceService instanceService;

    public HypervisorResolver(final InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    public List<InstanceType> instance(@Source final List<HypervisorAllocationType> allocations) {
        List<Instance> instances = this.instanceService.getAllWithComputeIds(allocations.stream().map(HypervisorAllocationType::getServerComputeId).toList());
        return allocations.stream()
            .map(allocation -> instances.stream().filter(instance -> allocation.getServerComputeId().equals(instance.getComputeId())).findFirst().orElse(null))
            .map(instance -> instance != null ? new InstanceType(instance) : null)
            .toList();
    }

}
