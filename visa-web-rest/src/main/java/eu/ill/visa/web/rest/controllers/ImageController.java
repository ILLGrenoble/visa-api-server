package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.ImageService;
import eu.ill.visa.core.entity.Image;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/images")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Authenticated
public class ImageController extends AbstractController {


    private final ImageService imageService;

    @Inject
    ImageController(final ImageService imageService) {
        this.imageService = imageService;
    }

    @GET
    public MetaResponse<List<Image>> getAll() {
        final List<Image> images = imageService.getAll();
        return createResponse(images);
    }

    @Path("/{image}")
    @GET
    public MetaResponse<Image> get(@PathParam("image") Image image) {
        return createResponse(image);
    }
}
