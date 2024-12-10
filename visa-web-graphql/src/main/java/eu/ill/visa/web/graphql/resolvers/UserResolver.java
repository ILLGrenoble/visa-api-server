package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.ExperimentService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.domain.filters.ExperimentFilter;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.web.graphql.types.ExperimentType;
import eu.ill.visa.web.graphql.types.InstanceType;
import eu.ill.visa.web.graphql.types.UserType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;

@RegisterForReflection
@GraphQLApi
public class UserResolver {

    private final InstanceService instanceService;
    private final ExperimentService experimentService;
    private final UserService userService;

    @Inject
    public UserResolver(final InstanceService instanceService,
                        final ExperimentService experimentService,
                        final UserService userService) {
        this.instanceService = instanceService;
        this.experimentService = experimentService;
        this.userService = userService;
    }

    public List<InstanceType> instances(@Source UserType userType) {
        final User user = userService.getById(userType.getId());
        return instanceService.getAllForUser(user, List.of()).stream()
            .map(InstanceType::new)
            .toList();
    }

    public List<ExperimentType> experiments(@Source UserType userType) {
        ExperimentFilter filter = new ExperimentFilter();
        filter.setUserId(userType.getId());
        return experimentService.getAll(filter).stream()
            .map(ExperimentType::new)
            .toList();
    }
}
