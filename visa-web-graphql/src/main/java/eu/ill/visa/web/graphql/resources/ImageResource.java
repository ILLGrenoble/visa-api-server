package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.business.services.CloudProviderService;
import eu.ill.visa.business.services.ImageProtocolService;
import eu.ill.visa.business.services.ImageService;
import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.core.entity.Image;
import eu.ill.visa.core.entity.ImageProtocol;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.ImageInput;
import eu.ill.visa.web.graphql.types.ImageType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.ArrayList;
import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class ImageResource {

    private final ImageService imageService;
    private final ImageProtocolService imageProtocolService;
    private final CloudClientService cloudClientService;
    private final CloudProviderService cloudProviderService;

    @Inject
    public ImageResource(final ImageService imageService,
                         final ImageProtocolService imageProtocolService,
                         final CloudClientService cloudClientService,
                         final CloudProviderService cloudProviderService) {
        this.imageService = imageService;
        this.imageProtocolService = imageProtocolService;
        this.cloudClientService = cloudClientService;
        this.cloudProviderService = cloudProviderService;
    }

    /**
     * Get a list of images
     *
     * @return a list of images
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public @NotNull List<ImageType> images() throws DataFetchingException {
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
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countImages() throws DataFetchingException {
        try {
            return imageService.countAllForAdmin();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    /**
     * Create a new image
     *
     * @param input the image properties
     * @return the newly created image
     */
    @Mutation
    public @NotNull ImageType createImage(@NotNull @Valid ImageInput input) throws EntityNotFoundException, InvalidInputException {
        // Validate the image input
        this.validateImageInput(input);

        final Image image = new Image();
        this.mapToImage(input, image);
        image.setDeleted(false);
        imageService.save(image);
        return new ImageType(image);
    }

    /**
     * Update a new image
     *
     * @param id    the image id
     * @param input the image properties
     * @return the newly created image
     */
    @Mutation
    public @NotNull ImageType updateImage(@NotNull @AdaptToScalar(Scalar.Int.class) Long id, @NotNull @Valid ImageInput input) throws EntityNotFoundException, InvalidInputException  {
        // Validate the image input
        this.validateImageInput(input);

        final Image image = imageService.getById(id);
        if (image == null) {
            throw new EntityNotFoundException("Image was not found for the given id");
        }
        this.mapToImage(input, image);
        imageService.save(image);
        return new ImageType(image);
    }

    /**
     * Delete a image for a given id
     *
     * @param id the image id
     * @return the deleted flavour
     * @throws EntityNotFoundException thrown if the image is not found
     */
    @Mutation
    public @NotNull ImageType deleteImage(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException {
        final Image image = imageService.getById(id);
        if (image == null) {
            throw new EntityNotFoundException("Image not found for the given id");
        }
        image.setDeleted(true);
        imageService.save(image);
        return new ImageType(image);
    }

    private void validateImageInput(ImageInput imageInput) throws InvalidInputException {
        try {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(imageInput.getCloudId());
            if (cloudClient == null) {
                throw new InvalidInputException("Invalid cloud Id");
            }

            CloudImage cloudImage = cloudClient.image(imageInput.getComputeId());
            if (cloudImage == null) {
                throw new InvalidInputException("Invalid Cloud Image Id");
            }

        } catch (CloudException exception) {
            throw new InvalidInputException("Error accessing Cloud");
        }
    }

    private void mapToImage(ImageInput input, Image image) throws EntityNotFoundException {
        image.setName(input.getName());
        image.setVersion(input.getVersion());
        image.setDescription(input.getDescription());
        image.setIcon(input.getIcon());
        image.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));
        image.setComputeId(input.getComputeId());
        image.setVisible(input.getVisible());
        image.setBootCommand(input.getBootCommand());
        image.setAutologin(input.getAutologin());
        final List<Long> protocolsId = input.getProtocolIds();
        final List<ImageProtocol> protocols = new ArrayList<>();
        for (Long protocolId : protocolsId) {
            final ImageProtocol protocol = imageProtocolService.getById(protocolId);
            if (protocol == null) {
                throw new EntityNotFoundException("Protocol not found for the given id");
            }
            protocols.add(protocol);
        }
        image.setProtocols(protocols);
    }

    private CloudProviderConfiguration getCloudProviderConfiguration(Long cloudId) {
        if (cloudId != null && cloudId > 0) {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(cloudId);
            return this.cloudProviderService.getById(cloudClient.getId());
        }
        return null;
    }

}
