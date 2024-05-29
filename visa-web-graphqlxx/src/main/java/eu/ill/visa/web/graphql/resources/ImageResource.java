package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.ImageService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.types.ImageType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class ImageResource {

    private final ImageService imageService;

    @Inject
    public ImageResource(final ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Get a list of images
     *
     * @return a list of images
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public List<ImageType> images() throws DataFetchingException {
        try {
            return this.imageService.getAllForAdmin().stream()
                .map(ImageType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Count all images
     *
     * @return a count of images
     * @throws DataFetchingException thrown if there was an error fetching the result
     */
    @Query
    public @AdaptToScalar(Scalar.Int.class) Long countImages() throws DataFetchingException {
        try {
            return imageService.countAllForAdmin();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }
}
