package eu.ill.visa.web.graphql.queries.resolvers.fields;

import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.core.entity.InstanceSessionMember;
import graphql.kickstart.tools.GraphQLResolver;

import java.util.Date;

@ApplicationScoped
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
