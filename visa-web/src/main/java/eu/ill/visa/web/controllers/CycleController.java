package eu.ill.visa.web.controllers;

import eu.ill.visa.business.services.CycleService;
import eu.ill.visa.core.domain.Cycle;
import eu.ill.visa.web.dtos.CycleDto;
import io.swagger.annotations.*;
import org.dozer.Mapper;

import javax.annotation.security.PermitAll;

import com.google.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;

@Path("/cycles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(tags = {"Cycles"}, description = "Cycle operations")
@PermitAll
public class CycleController extends AbstractController {

    private final CycleService cycleService;
    private final Mapper       mapper;

    @Inject
    public CycleController(final CycleService cycleService, final Mapper mapper) {
        this.cycleService = cycleService;
        this.mapper = mapper;
    }

    @GET
    @ApiOperation(value = "Get all cycles")
    public Response all() {
        final List<CycleDto> cycles = new ArrayList<>();
        for (final Cycle cycle : cycleService.getAll()) {
            cycles.add(mapper.map(cycle, CycleDto.class));
        }
        return createResponse(cycles, OK);
    }

    @GET
    @Path("/{cycle}")
    @ApiOperation(value = "Get a cycle")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Invalid ID supplied"),
        @ApiResponse(code = 404, message = "Cycle not found")
    })
    public Response get(@ApiParam(value = "Cycle identifier", name = "cycle", required = true)
                        @PathParam("cycle") final Cycle cycle) {
        return createResponse(mapper.map(cycle, CycleDto.class), OK);
    }
}
