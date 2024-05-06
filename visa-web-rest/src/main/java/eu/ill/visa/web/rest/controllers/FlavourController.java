package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.FlavourService;
import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/flavours")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Authenticated
public class FlavourController extends AbstractController {

    private final FlavourService flavourService;

    @Inject
    FlavourController(final FlavourService flavourService) {
        this.flavourService = flavourService;
    }

    @GET
    public MetaResponse<List<Flavour>> getAll() {
        final List<Flavour> flavours = flavourService.getAll();
        return createResponse(flavours);
    }

    @Path("/{flavour}")
    @GET
    public MetaResponse<Flavour> get(@PathParam("flavour") Flavour flavour) {
        return createResponse(flavour);
    }
}
