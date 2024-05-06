package eu.ill.visa.web.rest.controllers;

import com.github.dozermapper.core.Mapper;
import eu.ill.visa.business.services.InstrumentService;
import eu.ill.visa.core.entity.Instrument;
import eu.ill.visa.web.rest.dtos.InstrumentDto;
import eu.ill.visa.web.rest.module.MetaResponse;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/instruments")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Authenticated
public class InstrumentController extends AbstractController {

    private final InstrumentService instrumentService;
    private final Mapper mapper;

    @Inject
    public InstrumentController(final InstrumentService instrumentService,
                                final Mapper mapper) {
        this.instrumentService = instrumentService;
        this.mapper = mapper;
    }

    @GET
    public MetaResponse<List<InstrumentDto>> all() {
        final List<InstrumentDto> instruments = new ArrayList<>();
        for (final Instrument instrument : instrumentService.getAll()) {
            instruments.add(mapper.map(instrument, InstrumentDto.class));
        }
        return createResponse(instruments);
    }

    @GET
    @Path("/{instrument}")
    public MetaResponse<InstrumentDto> get(@PathParam("instrument") final Instrument instrument) {
        return createResponse(mapper.map(instrument, InstrumentDto.class));
    }
}
