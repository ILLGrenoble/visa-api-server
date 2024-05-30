package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.ImageProtocolService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.types.ImageProtocolType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class ImageProtocolResource {

    private final ImageProtocolService imageProtocolService;

    @Inject
    public ImageProtocolResource(final ImageProtocolService imageProtocolService) {
        this.imageProtocolService = imageProtocolService;
    }

    @Query
    public List<ImageProtocolType> imageProtocols() {
        return imageProtocolService.getAll().stream().map(ImageProtocolType::new).toList();
    }
}
