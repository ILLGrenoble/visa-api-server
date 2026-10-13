package eu.ill.visa.web.rest.controllers.vdi;

import eu.ill.visa.business.InvalidTokenException;
import eu.ill.visa.business.services.SignatureService;
import eu.ill.visa.business.vdi.VdiProviderService;
import eu.ill.visa.business.vdi.exceptions.SessionConnectionException;
import eu.ill.visa.common.vdi.apiconnect.ClientActivityInput;
import eu.ill.visa.common.vdi.apiconnect.ClientConnectionInput;
import eu.ill.visa.common.vdi.apiconnect.ClientDisconnectionInput;
import eu.ill.visa.common.vdi.apiconnect.VdiConnectionSignature;
import eu.ill.visa.common.vdi.domain.VdiAuthenticationToken;
import eu.ill.visa.common.vdi.domain.VdiRttSample;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.rest.controllers.AbstractController;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Path("/vdi")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(Role.VDI_ACCESS_ROLE)
public class VdiAccessController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(VdiAccessController.class);

    private final VdiProviderService vdiProviderService;
    private final SignatureService signatureService;

    @Inject
    public VdiAccessController(final VdiProviderService vdiProviderService,
                               final SignatureService signatureService) {
        this.vdiProviderService = vdiProviderService;
        this.signatureService = signatureService;
    }

    @GET
    @Path("/authenticate/{protocol}/{token}")
    public VdiAuthenticationToken authenticate(final @PathParam("protocol") String protocol, final @PathParam("token") String token) {
        if (!Arrays.asList(InstanceSession.WEBX_PROTOCOL, InstanceSession.GUACAMOLE_PROTOCOL).contains(protocol)) {
            throw new BadRequestException("invalid protocol");
        }

        try {
            return this.vdiProviderService.authenticate(protocol, token);

        } catch (InvalidTokenException exception) {
            logger.error("Token for initialising RemoteDesktopConnection is invalid: {}", exception.getMessage());
            throw new NotAuthorizedException(exception.getMessage());
        }
    }

    @GET
    @Path("/signature/{username}")
    public VdiConnectionSignature sign(final @PathParam("username") String username) {
        String signature = this.signatureService.createSignature(username);

        return new VdiConnectionSignature(username, signature);
    }

    @POST
    @Path("/session/connect")
    public void sessionConnect(ClientConnectionInput input) {
        try {
            this.vdiProviderService.onSessionConnect(input.instanceId(), input.protocol(), input.connectionId(), input.clientId(), input.user());

        } catch (SessionConnectionException exception) {
            throw new BadRequestException(exception.getMessage());
        }
    }

    @POST
    @Path("/session/disconnect")
    public void sessionDisconnect(ClientDisconnectionInput input) {
        this.vdiProviderService.onSessionDisconnect(input.connectionId(), input.clientId(), input.user());

    }

    @POST
    @Path("/session/failure")
    public void sessionFailure(ClientConnectionInput input) {
        this.vdiProviderService.onSessionFailure(input.instanceId(), input.protocol(), input.connectionId(), input.clientId(), input.user());
    }

    @POST
    @Path("/session/activity")
    public void sessionActivity(ClientActivityInput input) {
        this.vdiProviderService.onSessionActivity(input.instanceId(), input.connectionId(), input.clientId(), input.user(), input.lastSeenAt(), input.lastInteractionAt(), input.instanceActivityType());
    }

    @POST
    @Path("/session/rtt")
    public void sessionRtt(List<VdiRttSample> samples) {
        this.vdiProviderService.onSessionRttData(samples);
    }


}
