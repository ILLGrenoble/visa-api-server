package eu.ill.visa.app.vdi;


import eu.ill.visa.business.services.SignatureService;
import eu.ill.visa.business.vdi.VdiProviderService;
import eu.ill.visa.common.vdi.apiconnect.exceptions.InvalidTokenException;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.VdiAuthenticationToken;
import eu.ill.visa.common.vdi.domain.VdiRttSample;
import eu.ill.visa.common.vdi.domain.enums.InstanceActivityType;
import eu.ill.visa.vdi.apiconnect.exceptions.ApiCommunicationException;
import eu.ill.visa.vdi.business.services.VisaApiService;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.security.SignatureException;
import java.util.Date;
import java.util.List;

@LookupIfProperty(name = "vdi.isMicroservice", stringValue = "false")
@ApplicationScoped
public class VisaApiLocalService implements VisaApiService {


    private final VdiProviderService vdiProviderService;
    private final SignatureService signatureService;

    @Inject
    public VisaApiLocalService(final VdiProviderService vdiProviderService,
                               final SignatureService signatureService) {
        this.vdiProviderService = vdiProviderService;
        this.signatureService = signatureService;
    }

    @Override
    public VdiAuthenticationToken authenticate(String vdiProtocol, String token) throws InvalidTokenException {
        return this.vdiProviderService.authenticate(vdiProtocol, token);
    }

    @Override
    public String createVdiConnectionSignature(String username) throws SignatureException {
        return this.signatureService.createSignature(username);
    }

    @Override
    public void onClientConnection(Long instanceId, String protocol, String connectionId, String clientId, ConnectedUser connectedUser) throws ApiCommunicationException {
        this.vdiProviderService.onSessionConnect(instanceId, protocol, connectionId, clientId, connectedUser);
    }

    @Override
    public void onClientDisconnection(String connectionId, String clientId, ConnectedUser connectedUser) throws ApiCommunicationException {
        this.vdiProviderService.onSessionDisconnect(connectionId, clientId, connectedUser);
    }

    @Override
    public void onSessionConnectionFailed(Long instanceId, String protocol, String connectionId, String clientId, ConnectedUser connectedUser) throws ApiCommunicationException {
        this.vdiProviderService.onSessionFailure(instanceId, protocol, connectionId, clientId, connectedUser);
    }

    @Override
    public void updateClientActivity(Long instanceId, String connectionId, String clientId, ConnectedUser connectedUser, Date lastSeenAt, Date lastInteractionAt, InstanceActivityType instanceActivityType) throws ApiCommunicationException {
        this.vdiProviderService.onSessionActivity(instanceId, connectionId, clientId, connectedUser, lastSeenAt, lastInteractionAt, instanceActivityType);
    }

    @Override
    public void createDesktopSessionRttSamples(List<VdiRttSample> samples) throws ApiCommunicationException {
        this.vdiProviderService.onSessionRttData(samples);
    }
}
