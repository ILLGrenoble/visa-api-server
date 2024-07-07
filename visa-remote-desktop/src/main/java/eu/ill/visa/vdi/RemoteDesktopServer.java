package eu.ill.visa.vdi;

import com.corundumstudio.socketio.SocketIOServer;
import eu.ill.visa.business.services.InstanceActivityService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.business.services.TokenAuthenticatorService;
import eu.ill.visa.vdi.domain.models.SessionEvent;
import eu.ill.visa.vdi.gateway.dispatcher.ClientEventsGateway;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;
import eu.ill.visa.vdi.gateway.events.AccessRevokedEvent;
import eu.ill.visa.vdi.gateway.listeners.ClientConnectListener;
import eu.ill.visa.vdi.gateway.listeners.ClientDisconnectListener;
import eu.ill.visa.vdi.gateway.listeners.GuacamoleClientDisplayListener;
import eu.ill.visa.vdi.gateway.listeners.WebXClientDisplayListener;
import eu.ill.visa.vdi.gateway.subscribers.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RemoteDesktopServer {

    private final SocketIOServer server;
    private final DesktopSessionService desktopConnectionService;
    private final InstanceService instanceService;
    private final TokenAuthenticatorService authenticator;
    private final InstanceSessionService instanceSessionService;
    private final InstanceActivityService instanceActivityService;
    private final DesktopAccessService desktopAccessService;
    private final VirtualDesktopConfiguration virtualDesktopConfiguration;
    private final ClientEventsGateway clientEventsGateway;

    @Inject
    public RemoteDesktopServer(final SocketIOServer server,
                               final InstanceSessionService instanceSessionService,
                               final DesktopSessionService desktopConnectionService,
                               final InstanceService instanceService,
                               final TokenAuthenticatorService authenticator,
                               final DesktopAccessService desktopAccessService,
                               final VirtualDesktopConfiguration virtualDesktopConfiguration,
                               final InstanceActivityService instanceActivityService,
                               final ClientEventsGateway clientEventsGateway) {
        this.server = server;
        this.instanceSessionService = instanceSessionService;
        this.desktopConnectionService = desktopConnectionService;
        this.instanceService = instanceService;
        this.authenticator = authenticator;
        this.desktopAccessService = desktopAccessService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;
        this.instanceActivityService = instanceActivityService;
        this.clientEventsGateway = clientEventsGateway;
    }

    public void startServer() {
        if (this.virtualDesktopConfiguration.cleanupSessionsOnStartup()) {
            this.cleanupSessions();
        }

        this.bindListeners(server);
        this.bindClientSubscribers();
        this.server.start();
    }

    public void stopServer() {
        this.server.stop();
        // make sure the store factory has been shutdown
        this.server.getConfiguration().getStoreFactory().shutdown();
    }

    private void bindListeners(final SocketIOServer server) {
        server.addConnectListener(new ClientConnectListener(this.desktopConnectionService, this.desktopAccessService, this.instanceService, this.instanceSessionService, this.authenticator));
        server.addEventListener("display", String.class, new GuacamoleClientDisplayListener(this.desktopConnectionService, this.instanceService, this.instanceSessionService, this.instanceActivityService));
        server.addEventListener("webxdisplay", byte[].class, new WebXClientDisplayListener(this.desktopConnectionService, this.instanceService, this.instanceSessionService, this.instanceActivityService));
        server.addDisconnectListener(new ClientDisconnectListener(this.desktopConnectionService, this.desktopAccessService));
    }


    private void bindClientSubscribers() {
        this.clientEventsGateway.addConnectSubscriber(new ClientEventsConnectSubscriber(this.desktopConnectionService, this.instanceSessionService, this.authenticator));
        this.clientEventsGateway.addDisconnectSubscriber(new ClientEventsDisconnectSubscriber(this.desktopConnectionService));

        this.clientEventsGateway.subscribe("thumbnail", String.class)
            .next(new ClientThumbnailSubscriber(this.desktopConnectionService, this.instanceService));
        this.clientEventsGateway.subscribe(SessionEvent.ACCESS_REPLY_EVENT, AccessRequestResponseEvent.class)
            .next(new ClientAccessRequestResponseSubscriber(this.desktopAccessService));
        this.clientEventsGateway.subscribe(SessionEvent.ACCESS_REVOKED_EVENT, AccessRevokedEvent.class)
            .next(new ClientAccessRevokedSubscriber(this.desktopConnectionService));
    }

    private void cleanupSessions() {
        this.instanceSessionService.cleanupSession();
    }

}
