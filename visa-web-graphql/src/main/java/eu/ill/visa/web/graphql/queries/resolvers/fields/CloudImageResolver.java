package eu.ill.visa.web.graphql.queries.resolvers.fields;

import eu.ill.visa.cloud.domain.CloudImage;
import graphql.kickstart.tools.GraphQLResolver;
import jakarta.enterprise.context.ApplicationScoped;

import static java.lang.Math.toIntExact;

@ApplicationScoped
public class CloudImageResolver implements GraphQLResolver<CloudImage> {

    Integer size(CloudImage image) {
        if(image.getSize() == null) {
            return 0;
        }
        return toIntExact(image.getSize() / 1024 / 1024);
    }
}
