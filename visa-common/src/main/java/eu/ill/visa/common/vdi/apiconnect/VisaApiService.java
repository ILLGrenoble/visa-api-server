package eu.ill.visa.common.vdi.apiconnect;


import eu.ill.visa.common.vdi.apiconnect.exceptions.InvalidTokenException;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.VdiAuthenticationToken;
import eu.ill.visa.common.vdi.domain.VdiRttSample;
import eu.ill.visa.common.vdi.domain.enums.InstanceActivityType;
import eu.ill.visa.common.vdi.apiconnect.exceptions.ApiCommunicationException;

import java.security.SignatureException;
import java.util.Date;
import java.util.List;

public interface VisaApiService {

    VdiAuthenticationToken authenticate(final String vdiProtocol, final String token) throws InvalidTokenException;

    String createVdiConnectionSignature(final String username) throws SignatureException;

    void onClientConnection(final Long instanceId, final String protocol, final String connectionId, final String clientId, final ConnectedUser connectedUser) throws ApiCommunicationException;

    void onClientDisconnection(final String connectionId, final String clientId, final ConnectedUser connectedUser) throws ApiCommunicationException;

    void onSessionConnectionFailed(final Long instanceId, final String protocol, final String connectionId, final String clientId, final ConnectedUser connectedUser) throws ApiCommunicationException;

    void updateClientActivity(final Long instanceId, final String connectionId, final String clientId, final ConnectedUser connectedUser, final Date lastSeenAt, final Date lastInteractionAt, final InstanceActivityType instanceActivityType) throws ApiCommunicationException;

    void createDesktopSessionRttSamples(final List<VdiRttSample> samples) throws ApiCommunicationException;
}
