package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.DesktopSessionRttSampleService;
import eu.ill.visa.web.graphql.types.DesktopSessionRttSampleType;
import eu.ill.visa.web.graphql.types.InstanceSessionMemberType;
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

    @Inject
    public InstanceSessionMemberResolver(final DesktopSessionRttSampleService desktopSessionRttSampleService) {
        this.desktopSessionRttSampleService = desktopSessionRttSampleService;
    }

    public @NotNull List<List<DesktopSessionRttSampleType>> rttSamples(@Source List<InstanceSessionMemberType> instanceSessionMembers) {
        return this.desktopSessionRttSampleService.getByInstanceSessionMemberIds(instanceSessionMembers.stream().map(InstanceSessionMemberType::getId).toList()).stream()
            .map(samples -> samples.stream()
                .map(DesktopSessionRttSampleType::new)
                .toList()
            )
            .toList();
    }

}

