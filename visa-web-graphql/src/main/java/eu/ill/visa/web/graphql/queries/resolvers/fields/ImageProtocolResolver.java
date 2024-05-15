package eu.ill.visa.web.graphql.queries.resolvers.fields;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.core.entity.ImageProtocol;
import graphql.kickstart.tools.GraphQLResolver;

import static eu.ill.visa.business.services.PortService.isPortOpen;

@ApplicationScoped
public class ImageProtocolResolver implements GraphQLResolver<ImageProtocol> {

    @Inject
    public ImageProtocolResolver() {
    }

    boolean isUp(ImageProtocol protocol) {
        final Integer port = protocol.getPort();
        return isPortOpen("localhost", port);
    }
}
