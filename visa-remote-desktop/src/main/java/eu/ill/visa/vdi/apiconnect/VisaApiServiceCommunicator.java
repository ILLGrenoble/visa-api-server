package eu.ill.visa.vdi.apiconnect;


import eu.ill.visa.common.vdi.apiconnect.VisaApiService;
import eu.ill.visa.common.vdi.apiconnect.exceptions.ApiCommunicationException;
import eu.ill.visa.common.vdi.apiconnect.exceptions.InvalidTokenException;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.VdiAuthenticationToken;
import eu.ill.visa.common.vdi.domain.VdiRttSample;
import eu.ill.visa.common.vdi.domain.enums.InstanceActivityType;
import eu.ill.visa.vdi.apiconnect.messages.*;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SignatureException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static eu.ill.visa.vdi.apiconnect.messages.AsyncApiMessage.Type.*;

@ApplicationScoped
public class VisaApiServiceCommunicator {


    private static final Logger logger = LoggerFactory.getLogger(VisaApiServiceCommunicator.class);

    private final VisaApiService visaApiService;
    private final Thread communicator;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final LinkedBlockingDeque<AsyncApiMessage> messageQueue = new LinkedBlockingDeque<>();

    public VisaApiServiceCommunicator(final jakarta.enterprise.inject.Instance<VisaApiService> visaApiServiceInstance) {
        this.visaApiService = visaApiServiceInstance.get();
        this.communicator = new Thread(this::threadMain);
    }

    public void start() {
        this.running.set(true);
        communicator.start();
    }

    public void stop() {
        try {
            this.running.set(true);
            communicator.interrupt();
            communicator.join(Duration.ofMillis(1000));

        } catch (InterruptedException e) {
            logger.error("Interrupted while stopping VISA API Service communicator");
        }
    }

    public VdiAuthenticationToken authenticate(String vdiProtocol, String token) throws InvalidTokenException {
        return this.visaApiService.authenticate(vdiProtocol, token);
    }

    public String createVdiConnectionSignature(String username) throws SignatureException {
        return this.visaApiService.createVdiConnectionSignature(username);
    }

    public void onClientConnection(Long instanceId, String protocol, String connectionId, String clientId, ConnectedUser connectedUser) throws ApiCommunicationException {
        this.visaApiService.onClientConnection(instanceId, protocol, connectionId, clientId, connectedUser);
    }

    public void onClientDisconnection(String connectionId, String clientId, ConnectedUser connectedUser) {
        this.messageQueue.push(new DisconnectAsyncApiMessage(connectionId, clientId, connectedUser));
    }

    public void onSessionConnectionFailed(Long instanceId, String protocol, String connectionId, String clientId, ConnectedUser connectedUser) {
        this.messageQueue.push(new ConnectionFailedAsyncApiMessage(instanceId, protocol, connectionId, clientId, connectedUser));
    }

    public void updateClientActivity(Long instanceId, String connectionId, String clientId, ConnectedUser connectedUser, Date lastSeenAt, Date lastInteractionAt, InstanceActivityType instanceActivityType) {
        this.messageQueue.push(new UpdateClientActivityAsyncApiMessage(instanceId, connectionId, clientId, connectedUser, lastSeenAt, lastInteractionAt, instanceActivityType));
    }

    public void createDesktopSessionRttSamples(List<VdiRttSample> samples) {
        this.messageQueue.push(new CreateRttSamplesAsyncApiMessage(samples));
    }


    public void threadMain() {

        while (running.get()) {
            try {
                final AsyncApiMessage message = this.messageQueue.poll(50, TimeUnit.MILLISECONDS);
                if (message != null && running.get()) {
                    this.handleMessage(message);
                }

            } catch (InterruptedException exception) {
                if (running.get()) {
                    logger.error("Interrupted while waiting for async VISA API Messages to process", exception);
                }
            }
        }
    }

    private void handleMessage(final AsyncApiMessage message) {
        try {
            if (message.type().equals(DISCONNECT)) {
                this.handleDisconnectMessage((DisconnectAsyncApiMessage) message);

            } else if (message.type().equals(CONNECTION_FAILED)) {
                this.handleConnectionFailedMessage((ConnectionFailedAsyncApiMessage) message);

            } else if (message.type().equals(UPDATE_CLIENT_ACTIVITY)) {
                this.handleUpdateClientActivityMessage((UpdateClientActivityAsyncApiMessage) message);

            } else if (message.type().equals(CREATE_RTT_SAMPLES)) {
                this.handleCreateRttSamplesMessage((CreateRttSamplesAsyncApiMessage) message);

            } else {
                logger.warn("Unknown Async API Message of type {}",  message.type());
            }

        } catch (ApiCommunicationException error) {
            logger.error(message.formatError(error));
        }

    }

    private void handleDisconnectMessage(final DisconnectAsyncApiMessage message) throws ApiCommunicationException {
        this.visaApiService.onClientDisconnection(message.connectionId(), message.clientId(), message.connectedUser());
    }

    private void handleConnectionFailedMessage(final ConnectionFailedAsyncApiMessage message) throws ApiCommunicationException {
        this.visaApiService.onSessionConnectionFailed(message.instanceId(), message.protocol(), message.connectionId(), message.clientId(), message.connectedUser());
    }

    private void handleUpdateClientActivityMessage(final UpdateClientActivityAsyncApiMessage message) throws ApiCommunicationException {
        this.visaApiService.updateClientActivity(message.instanceId(), message.connectionId(), message.clientId(), message.connectedUser(), message.lastSeenAt(), message.lastInteractionAt(), message.instanceActivityType());
    }

    private void handleCreateRttSamplesMessage(final CreateRttSamplesAsyncApiMessage message) throws ApiCommunicationException {
        this.visaApiService.createDesktopSessionRttSamples(message.samples());
    }

}
