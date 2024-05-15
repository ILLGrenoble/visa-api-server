package eu.ill.visa.web.graphql.queries.resolvers.fields;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.business.services.ExperimentService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Experiment;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import graphql.kickstart.tools.GraphQLResolver;

import java.util.List;

@ApplicationScoped
public class UserResolver implements GraphQLResolver<User> {

    private final InstanceService instanceService;
    private final ExperimentService experimentService;

    @Inject
    public UserResolver(final InstanceService instanceService,
                        final ExperimentService experimentService) {
        this.instanceService = instanceService;
        this.experimentService = experimentService;
    }

    public List<Instance> instances(User user) {
        return instanceService.getAllForUser(user);
    }

    public List<Experiment> experiments(User user) {
        return experimentService.getAllForUser(user);
    }
}
