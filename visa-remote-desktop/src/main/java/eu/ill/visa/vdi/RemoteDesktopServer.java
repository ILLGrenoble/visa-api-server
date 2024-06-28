package eu.ill.visa.vdi;

import com.corundumstudio.socketio.SocketIOServer;
import eu.ill.visa.business.services.InstanceActivityService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.business.services.TokenAuthenticatorService;
import eu.ill.visa.vdi.domain.models.SessionEvent;
import eu.ill.visa.vdi.gateway.dispatcher.ClientEventDispatcher;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;
import eu.ill.visa.vdi.gateway.events.AccessRevokedEvent;
import eu.ill.visa.vdi.gateway.events.ClientEventCarrier;
import eu.ill.visa.vdi.gateway.listeners.*;
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
    private final ClientEventDispatcher clientEventDispatcher;

    @Inject
    public RemoteDesktopServer(final SocketIOServer server,
                               final InstanceSessionService instanceSessionService,
                               final DesktopSessionService desktopConnectionService,
                               final InstanceService instanceService,
                               final TokenAuthenticatorService authenticator,
                               final DesktopAccessService desktopAccessService,
                               final VirtualDesktopConfiguration virtualDesktopConfiguration,
                               final InstanceActivityService instanceActivityService,
                               final ClientEventDispatcher clientEventDispatcher) {
        this.server = server;
        this.instanceSessionService = instanceSessionService;
        this.desktopConnectionService = desktopConnectionService;
        this.instanceService = instanceService;
        this.authenticator = authenticator;
        this.desktopAccessService = desktopAccessService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;
        this.instanceActivityService = instanceActivityService;
        this.clientEventDispatcher = clientEventDispatcher;
    }

    public void startServer() {
        if (this.virtualDesktopConfiguration.cleanupSessionsOnStartup()) {
            this.cleanupSessions();
        }

        this.bindListeners(server);
        this.bindClientEventListeners();
        this.server.start();
    }

    public void stopServer() {
        this.server.stop();
        // make sure the store factory has been shutdown
        this.server.getConfiguration().getStoreFactory().shutdown();
    }

    private void bindListeners(final SocketIOServer server) {
        server.addConnectListener(new ClientConnectListener(this.desktopConnectionService, this.desktopAccessService, this.instanceService, this.instanceSessionService, this.authenticator));
        server.addEventListener("event", ClientEventCarrier.class, new ClientEventCarrierListener(this.clientEventDispatcher));
        server.addEventListener("display", String.class, new GuacamoleClientDisplayListener(this.desktopConnectionService, this.instanceService, this.instanceSessionService, this.instanceActivityService));
        server.addEventListener("webxdisplay", byte[].class, new WebXClientDisplayListener(this.desktopConnectionService, this.instanceService, this.instanceSessionService, this.instanceActivityService));
        server.addDisconnectListener(new ClientDisconnectListener(this.desktopConnectionService, this.desktopAccessService));
    }


    private void bindClientEventListeners() {
        this.clientEventDispatcher.subscribe("thumbnail", String.class)
            .next(new ClientThumbnailListener(this.desktopConnectionService, this.instanceService));
        this.clientEventDispatcher.subscribe(SessionEvent.ACCESS_REPLY_EVENT, AccessRequestResponseEvent.class)
            .next(new ClientAccessRequestResponseListener(this.desktopAccessService));
        this.clientEventDispatcher.subscribe(SessionEvent.ACCESS_REVOKED_EVENT, AccessRevokedEvent.class)
            .next(new ClientAccessRevokedListener(this.desktopConnectionService));
    }

    private void cleanupSessions() {
        this.instanceSessionService.cleanupSession();
    }

}
