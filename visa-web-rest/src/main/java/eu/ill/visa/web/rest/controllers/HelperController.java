package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.InstanceNameGeneratorService;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Path("/helpers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class HelperController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(HelperController.class);

    private final InstanceNameGeneratorService instanceNameGeneratorService;

    @Inject
    HelperController(final InstanceNameGeneratorService instanceNameGeneratorService) {
        this.instanceNameGeneratorService = instanceNameGeneratorService;
    }

    @GET
    @Path("/random_instance_name")
    public MetaResponse<String> get() {
        try {
            return createResponse(instanceNameGeneratorService.generate());
        } catch (IOException exception) {
            logger.error("Error generating a random instance name", exception);
            throw new InternalServerErrorException("An exception was thrown");
        }
    }
}
