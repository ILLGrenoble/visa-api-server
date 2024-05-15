package eu.ill.visa.web.graphql.queries.resolvers.fields;

import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.core.entity.InstanceJupyterSession;
import graphql.kickstart.tools.GraphQLResolver;

import java.util.Date;

@ApplicationScoped
public class InstanceJupyterSessionResolver implements GraphQLResolver<InstanceJupyterSession> {

    public Long duration(final InstanceJupyterSession instanceJupyterSession) {
        final long now = new Date().getTime();
        final long createdAt = instanceJupyterSession.getCreatedAt().getTime();
        final long updatedAt = instanceJupyterSession.getUpdatedAt().getTime();
        if (instanceJupyterSession.isActive()) {
            return (now - createdAt) / 1000;
        }
        return ((updatedAt - createdAt) / 1000);
    }
}
