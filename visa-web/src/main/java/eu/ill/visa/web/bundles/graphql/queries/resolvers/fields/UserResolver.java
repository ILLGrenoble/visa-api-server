package eu.ill.visa.web.bundles.graphql.queries.resolvers.fields;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import eu.ill.visa.business.services.ExperimentService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.domain.Experiment;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.User;
import graphql.kickstart.tools.GraphQLResolver;

import java.util.List;

@Singleton
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
