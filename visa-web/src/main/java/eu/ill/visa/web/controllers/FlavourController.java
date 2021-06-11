package eu.ill.visa.web.controllers;

import com.google.inject.Inject;
import eu.ill.visa.business.services.FlavourService;
import eu.ill.visa.core.domain.Flavour;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/flavours")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class FlavourController extends AbstractController {

    private final FlavourService flavourService;

    @Inject
    FlavourController(final FlavourService flavourService) {
        this.flavourService = flavourService;
    }

    @GET
    public Response getAll() {
        final List<Flavour> flavours = flavourService.getAll();
        return createResponse(flavours);
    }

    @Path("/{flavour}")
    @GET
    public Response get(@PathParam("flavour") Flavour flavour) {
        return createResponse(flavour);
    }
}
