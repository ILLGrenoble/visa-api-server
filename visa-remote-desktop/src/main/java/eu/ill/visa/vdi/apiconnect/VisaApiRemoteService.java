package eu.ill.visa.vdi.apiconnect;

import eu.ill.visa.common.vdi.apiconnect.ClientActivityInput;
import eu.ill.visa.common.vdi.apiconnect.ClientConnectionInput;
import eu.ill.visa.common.vdi.apiconnect.ClientDisconnectionInput;
import eu.ill.visa.common.vdi.apiconnect.VisaApiService;
import eu.ill.visa.common.vdi.apiconnect.exceptions.InvalidTokenException;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.VdiAuthenticationToken;
import eu.ill.visa.common.vdi.domain.VdiRttSample;
import eu.ill.visa.common.vdi.domain.enums.InstanceActivityType;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.common.vdi.apiconnect.exceptions.ApiCommunicationException;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SignatureException;
import java.util.Date;
import java.util.List;

import static eu.ill.visa.common.utils.exceptions.ExceptionFormatter.formatException;

@LookupIfProperty(name = "vdi.isMicroservice", stringValue = "true")
@ApplicationScoped
public class VisaApiRemoteService implements VisaApiService {
    private static final Logger logger = LoggerFactory.getLogger(VisaApiRemoteService.class);

    @RestClient
    VisaApiClient visaApiClient;

    public VisaApiRemoteService(final VirtualDesktopConfiguration configuration) {
        if (configuration.apiServerAuthToken().isEmpty()) {
            logger.error("No VISA API Server Auth Token has been specified in the configuration value vdi.apiServerAuthToken. This must be set using the VISA_VDI_API_SERVER_AUTH_TOKEN environment variable to enable security requests with the VISA API Server: remote desktop services will not be possible without it.");
        }
    }

    public VdiAuthenticationToken authenticate(final String vdiProtocol, final String token) throws InvalidTokenException {
        // Server will:
        // - Validate the token and obtain Instance/User info
        // - Generate Instance data for the VDI client
        // - Generate ConnectedUser data for the VDI client
        // - throw a authorization error if access not ok

        try {
            return this.visaApiClient.authenticate(vdiProtocol, token);

        } catch (UnauthorizedRuntimeException e) {
            throw new InvalidTokenException(e.getMessage());
        }
    }

    public String createVdiConnectionSignature(final String username) throws SignatureException {
        // Server will:
        // - Create a time-limited signature for the username

        try {
            return this.visaApiClient.sign(username).signature();

        } catch (Exception e) {
            logger.error("Failed to create VDI connection signature: {}", formatException(e));
            throw new SignatureException();
        }
    }

    public void onClientConnection(final Long instanceId, final String protocol, final String connectionId, final String clientId, final ConnectedUser connectedUser) throws ApiCommunicationException {
        // Server will:
        // - Find the InstanceSession associated with the connectionId and clientId
        // - Create InstanceSession if currently null (for the given connectionId)
        // - Create new InstanceSessionMember
        // - Determine if session is unlocked
        // - Send either session unlocked or user connected redis messages
        // - Send all connected Users redis message

        try {
            this.visaApiClient.sessionConnect(new ClientConnectionInput(instanceId, protocol, connectionId, clientId, connectedUser));

        } catch (Exception e) {
            throw new ApiCommunicationException(String.format("Failed to notify api server of client connection for user %s: %s", connectedUser, formatException(e)));
        }
    }

    public void onClientDisconnection(final String connectionId, final String clientId, final ConnectedUser connectedUser) throws ApiCommunicationException {
        // Server will:
        // - Find the InstanceSession associated with the connectionId and clientId
        // - Deactivated InstanceSessionMember (and InstanceSession if no one connected)
        // - Determine if session is locked or closed
        // - Send either session locked, session closed or user disconnected redis messages
        // - Send all connected users redis message

        try {
            this.visaApiClient.sessionDisconnect(new ClientDisconnectionInput(connectionId, clientId, connectedUser));

        } catch (Exception e) {
            throw new ApiCommunicationException(String.format("Failed to notify api server of client disconnection for user %s: %s", connectedUser, formatException(e)));
        }
    }

    public void onSessionConnectionFailed(final Long instanceId, final String protocol, final String connectionId, final String clientId, final ConnectedUser connectedUser) throws ApiCommunicationException {
        // Server will:
        // - Find the InstanceSession associated with the connectionId and clientId
        // - Make the InstanceSession inactive

        try {
            this.visaApiClient.sessionFailure(new ClientConnectionInput(instanceId, protocol, connectionId, clientId, connectedUser));

        } catch (Exception e) {
            throw new ApiCommunicationException(String.format("Failed to notify api server of session failure for user %s: %s", connectedUser, formatException(e)));
        }
    }

    public void updateClientActivity(final Long instanceId, final String connectionId, final String clientId, final ConnectedUser connectedUser, final Date lastSeenAt, final Date lastInteractionAt, final InstanceActivityType instanceActivityType) throws ApiCommunicationException {
        // Server will:
        // - Find the InstanceSession associated with the connectionId and clientId
        // - Update instance lastSeenAt and lastInteractionAt
        // - Update instanceSessionMember lastInteractionAt
        // - Create instance activity if (instanceActivityType is not null)

        try {
            this.visaApiClient.sessionActivity(new ClientActivityInput(instanceId, connectionId, clientId, connectedUser,  lastSeenAt, lastInteractionAt, instanceActivityType));

        } catch (Exception e) {
            throw new ApiCommunicationException(String.format("Failed to notify api server of session activity for user %s: %s", connectedUser, formatException(e)));
        }
    }

    public void createDesktopSessionRttSamples(final List<VdiRttSample> samples) throws ApiCommunicationException {
        // Server will:
        // - Fine InstanceSessionMember associated to clientId and InstanceSessionId of each sample
        // - Save the RTT data

        try {
            this.visaApiClient.sessionRtt(samples);

        } catch (Exception e) {
            throw new ApiCommunicationException(String.format("Failed to notify api server of session RTT samples: %s", formatException(e)));
        }
    }
}
