package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.DesktopSessionRttSampleService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.domain.SampleStats;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.web.graphql.types.HypervisorAllocationType;
import eu.ill.visa.web.graphql.types.HypervisorType;
import eu.ill.visa.web.graphql.types.InstanceType;
import eu.ill.visa.web.graphql.types.SampleStatsType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RegisterForReflection
@GraphQLApi
public class HypervisorResolver {

    private final InstanceService instanceService;
    private final DesktopSessionRttSampleService desktopSessionRttSampleService;


    public HypervisorResolver(final InstanceService instanceService,
                              final DesktopSessionRttSampleService desktopSessionRttSampleService) {
        this.instanceService = instanceService;
        this.desktopSessionRttSampleService = desktopSessionRttSampleService;
    }

    public List<InstanceType> instance(@Source final List<HypervisorAllocationType> allocations) {
        List<Instance> instances = this.instanceService.getAllWithComputeIds(allocations.stream().map(HypervisorAllocationType::getServerComputeId).toList());
        return allocations.stream()
            .map(allocation -> instances.stream().filter(instance -> allocation.getServerComputeId().equals(instance.getComputeId())).findFirst().orElse(null))
            .map(instance -> instance != null ? new InstanceType(instance) : null)
            .toList();
    }

    public List<List<SampleStatsType>> instanceRTTStats(@Source final List<HypervisorType> hypervisors) {
        Map<Long, List<SampleStats>> hypervisorSampleStats = this.desktopSessionRttSampleService.getRecentSampleStatsByHypervisor().entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().getId(),
                Map.Entry::getValue
        ));

        return hypervisors.stream()
            .map(hypervisor -> {
                List<SampleStats> samples = hypervisorSampleStats.get(hypervisor.getId());
                return samples != null ? samples.stream().map(SampleStatsType::new).toList() : new ArrayList<SampleStatsType>();
            })
            .toList();
    }

}
