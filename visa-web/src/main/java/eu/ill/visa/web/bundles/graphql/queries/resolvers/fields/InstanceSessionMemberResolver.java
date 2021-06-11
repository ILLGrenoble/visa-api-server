package eu.ill.visa.web.bundles.graphql.queries.resolvers.fields;

import com.google.inject.Singleton;
import eu.ill.visa.core.domain.InstanceSessionMember;
import graphql.kickstart.tools.GraphQLResolver;

import java.util.Date;

@Singleton
public class InstanceSessionMemberResolver implements GraphQLResolver<InstanceSessionMember> {

    public Long duration(final InstanceSessionMember instanceSessionMember) {
        final long now = new Date().getTime();
        final long createdAt = instanceSessionMember.getCreatedAt().getTime();
        final long updatedAt = instanceSessionMember.getUpdatedAt().getTime();
        if (instanceSessionMember.isActive()) {
            return (now - createdAt) / 1000;
        }
        return ((updatedAt - createdAt) / 1000);
    }
}
