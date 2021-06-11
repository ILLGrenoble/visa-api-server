package eu.ill.visa.web.bundles.graphql.queries.resolvers.fields;

import eu.ill.visa.cloud.domain.CloudImage;
import graphql.kickstart.tools.GraphQLResolver;

import static java.lang.Math.toIntExact;

public class CloudImageResolver implements GraphQLResolver<CloudImage> {

    Integer size(CloudImage image) {
        if(image.getSize() == null) {
            return 0;
        }
        return toIntExact(image.getSize() / 1024 / 1024);
    }
}
