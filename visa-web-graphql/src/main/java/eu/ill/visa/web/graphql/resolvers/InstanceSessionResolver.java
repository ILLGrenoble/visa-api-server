package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.domain.filters.InstanceFilter;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.web.graphql.types.InstanceSessionType;
import eu.ill.visa.web.graphql.types.InstanceType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;


@RegisterForReflection
@GraphQLApi
public class InstanceSessionResolver {

    private final InstanceService instanceService;

    @Inject
    public InstanceSessionResolver(final InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    public List<InstanceType> instance(@Source List<InstanceSessionType> instanceSessions) {
        final InstanceFilter instanceFilter = new InstanceFilter();
        instanceFilter.setIds(instanceSessions.stream().map(InstanceSessionType::getInstanceId).toList());
        List<Instance> instances = this.instanceService.getAll(instanceFilter);

        return instanceSessions.stream().map(instanceSessionType -> {
                return instances.stream().filter(instance -> instance.getId().equals(instanceSessionType.getInstanceId())).findFirst().orElse(null);
            })
            .map(instance -> instance == null ? null : new InstanceType(instance))
            .toList();
    }


}

