package eu.ill.visa.web.controllers;

import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstrumentService;
import eu.ill.visa.core.domain.Instrument;
import eu.ill.visa.web.dtos.InstrumentDto;
import org.dozer.Mapper;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;


@Path("/instruments")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
public class InstrumentController extends AbstractController {

    private final InstrumentService instrumentService;
    private final Mapper            mapper;

    @Inject
    public InstrumentController(final InstrumentService instrumentService,
                                final Mapper mapper) {
        this.instrumentService = instrumentService;
        this.mapper = mapper;
    }

    @GET
    public Response all() {
        final List<InstrumentDto> instruments = new ArrayList<>();
        for (final Instrument instrument : instrumentService.getAll()) {
            instruments.add(mapper.map(instrument, InstrumentDto.class));
        }
        return createResponse(instruments, OK);
    }

    @GET
    @Path("/{instrument}")
    public Response get(@PathParam("instrument") final Instrument instrument) {
        return createResponse(mapper.map(instrument, InstrumentDto.class), OK);
    }
}
