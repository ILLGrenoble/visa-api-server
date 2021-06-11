package eu.ill.visa.web.controllers;

import com.google.inject.Inject;
import eu.ill.visa.business.services.ImageService;
import eu.ill.visa.core.domain.Image;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/images")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class ImageController extends AbstractController {


    private final ImageService imageService;

    @Inject
    ImageController(final ImageService imageService) {
        this.imageService = imageService;
    }

    @GET
    public Response getAll() {
        final List<Image> images = imageService.getAll();
        return createResponse(images);
    }

    @Path("/{image}")
    @GET
    public Response get(@PathParam("image") Image image) {
        return createResponse(image);
    }
}
