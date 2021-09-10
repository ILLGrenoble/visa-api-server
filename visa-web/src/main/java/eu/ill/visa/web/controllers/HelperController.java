package eu.ill.visa.web.controllers;

import eu.ill.visa.business.services.InstanceNameGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/helpers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class HelperController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(HelperController.class);

    private final InstanceNameGeneratorService instanceNameGeneratorService;

    @Inject
    HelperController(final InstanceNameGeneratorService instanceNameGeneratorService) {
        this.instanceNameGeneratorService = instanceNameGeneratorService;
    }

    @GET
    @Path("/random_instance_name")
    public Response get() {
        try {
            return createResponse(instanceNameGeneratorService.generate());
        } catch (IOException exception) {
            logger.error("Error generating a random instance name", exception);
            throw new InternalServerErrorException("An exception was thrown");
        }
    }
}
