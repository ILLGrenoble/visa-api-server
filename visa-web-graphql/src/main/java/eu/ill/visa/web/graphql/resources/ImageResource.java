package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.*;
import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.entity.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class ImageResource {

    private static final Logger logger = LoggerFactory.getLogger(ImageResource.class);

    private final ImageService imageService;
    private final ImageProtocolService imageProtocolService;
    private final CloudClientService cloudClientService;
    private final CloudProviderService cloudProviderService;
    private final PlanService planService;

    @Inject
    public ImageResource(final ImageService imageService,
                         final ImageProtocolService imageProtocolService,
                         final CloudClientService cloudClientService,
                         final CloudProviderService cloudProviderService,
                         final PlanService planService) {
        this.imageService = imageService;
        this.imageProtocolService = imageProtocolService;
        this.cloudClientService = cloudClientService;
        this.cloudProviderService = cloudProviderService;
        this.planService = planService;
    }

    /**
     * Get a list of images
     *
     * @return a list of images
     */
    @Query
    public @NotNull List<ImageType> images() {
        return this.imageService.getAllForAdmin().stream()
            .map(ImageType::new)
            .toList();
    }

    /**
     * Count all images
     *
     * @return a count of images
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countImages() {
        return imageService.countAllForAdmin();
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

        // Clone plans if requested
        if (input.getClonePlansFromImageId() != null) {
            final Image clonedImage = imageService.getById(input.getClonePlansFromImageId());
            if (clonedImage == null) {
                logger.warn("Image with id {} was not found so unable to clone plans", input.getClonePlansFromImageId());
            } else {
                List<Plan> plans = this.planService.getAllForAdmin().stream()
                    .filter(plan -> plan.getImage().getId().equals(input.getClonePlansFromImageId()))
                    .toList();

                plans.forEach(plan -> {
                    final Plan clonedPlan = new Plan();
                    clonedPlan.setFlavour(plan.getFlavour());
                    clonedPlan.setImage(image);
                    clonedPlan.setPreset(false);
                    planService.create(clonedPlan);
                });
            }
        }

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

            ImageProtocol imageProtocol = imageProtocolService.getById(imageInput.getDefaultVdiProtocolId());
            if (imageProtocol == null) {
                throw new InvalidInputException("Invalid Default VDI Protocol Id");
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
        image.setDefaultVdiProtocol(imageProtocolService.getById(input.getDefaultVdiProtocolId()));
        image.setSecondaryVdiProtocol(input.getSecondaryVdiProtocolId() != null ? imageProtocolService.getById(input.getSecondaryVdiProtocolId()) : null);
        image.setExtensionRequestPolicy(input.getExtensionRequestPolicy());
    }

    private CloudProviderConfiguration getCloudProviderConfiguration(Long cloudId) {
        if (cloudId != null && cloudId > 0) {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(cloudId);
            return this.cloudProviderService.getById(cloudClient.getId());
        }
        return null;
    }

}
