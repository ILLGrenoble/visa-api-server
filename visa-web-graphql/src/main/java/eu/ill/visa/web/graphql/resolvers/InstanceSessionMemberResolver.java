package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.DesktopSessionRttSampleService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.domain.filters.InstanceFilter;
import eu.ill.visa.core.domain.filters.UserFilter;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.web.graphql.types.DesktopSessionRttSampleType;
import eu.ill.visa.web.graphql.types.InstanceDesktopSessionType;
import eu.ill.visa.web.graphql.types.InstanceType;
import eu.ill.visa.web.graphql.types.UserType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;


@RegisterForReflection
@GraphQLApi
public class InstanceSessionMemberResolver {

    private final DesktopSessionRttSampleService desktopSessionRttSampleService;
    private final InstanceService instanceService;
    private final UserService userService;

    @Inject
    public InstanceSessionMemberResolver(final DesktopSessionRttSampleService desktopSessionRttSampleService,
                                         final InstanceService instanceService,
                                         final UserService userService) {
        this.desktopSessionRttSampleService = desktopSessionRttSampleService;
        this.instanceService = instanceService;
        this.userService = userService;
    }

    public @NotNull List<List<DesktopSessionRttSampleType>> rttSamples(@Source List<InstanceDesktopSessionType> instanceSessionMembers) {
        return this.desktopSessionRttSampleService.getByInstanceSessionMemberIds(instanceSessionMembers.stream().map(InstanceDesktopSessionType::getId).toList()).stream()
            .map(samples -> samples.stream()
                .map(DesktopSessionRttSampleType::new)
                .toList()
            )
            .toList();
    }

    public @NotNull List<InstanceType> instance(@Source List<InstanceDesktopSessionType> instanceSessionMembers) {
        final InstanceFilter instanceFilter = new InstanceFilter();
        instanceFilter.setIds(instanceSessionMembers.stream().map(InstanceDesktopSessionType::getInstanceId).toList());
        List<Instance> instances = this.instanceService.getAll(instanceFilter);

        return instanceSessionMembers.stream().map(instanceDesktopSessionType -> {
                return instances.stream().filter(instance -> instance.getId().equals(instanceDesktopSessionType.getInstanceId())).findFirst().orElse(null);
            })
            .map(instance -> instance == null ? null : new InstanceType(instance))
            .toList();
    }

    public @NotNull List<UserType> user(@Source List<InstanceDesktopSessionType> instanceSessionMembers) {
        final UserFilter userFilter = new UserFilter();
        userFilter.setIds(instanceSessionMembers.stream().map(InstanceDesktopSessionType::getUserId).toList());

        List<User> users = this.userService.getAll(userFilter);

        return instanceSessionMembers.stream().map(instanceDesktopSessionType -> {
                return users.stream().filter(user -> user.getId().equals(instanceDesktopSessionType.getUserId())).findFirst().orElse(null);
            })
            .map(user -> user == null ? null : new UserType(user))
            .toList();
    }

}

