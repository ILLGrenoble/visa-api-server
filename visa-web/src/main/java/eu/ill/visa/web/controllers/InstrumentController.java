package eu.ill.visa.web.controllers;

import eu.ill.visa.business.services.InstrumentService;
import eu.ill.visa.core.domain.Instrument;
import eu.ill.visa.web.dtos.InstrumentDto;
import io.swagger.annotations.*;
import org.dozer.Mapper;

import javax.annotation.security.PermitAll;

import com.google.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;


@Path("/instruments")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Api(tags = {"Instruments"}, description = "Instrument operations")
@PermitAll
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
    @ApiOperation(value = "Get all instruments")
    public Response all() {
        final List<InstrumentDto> instruments = new ArrayList<>();
        for (final Instrument instrument : instrumentService.getAll()) {
            instruments.add(mapper.map(instrument, InstrumentDto.class));
        }
        return createResponse(instruments, OK);
    }

    @GET
    @Path("/{instrument}")
    @ApiOperation(value = "Get an instrument")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "Instrument not found")
    })
    public Response get(@ApiParam(value = "Instrument identifier", name = "instrument", required = true)
                        @PathParam("instrument") final Instrument instrument) {
        return createResponse(mapper.map(instrument, InstrumentDto.class), OK);
    }
}
