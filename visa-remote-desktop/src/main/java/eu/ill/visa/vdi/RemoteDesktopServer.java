package eu.ill.visa.vdi;

import com.corundumstudio.socketio.SocketIOServer;
import eu.ill.visa.business.services.InstanceActivityService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopConnectionService;
import eu.ill.visa.vdi.business.services.TokenAuthenticatorService;
import eu.ill.visa.vdi.domain.models.Event;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;
import eu.ill.visa.vdi.gateway.events.AccessRevokedEvent;
import eu.ill.visa.vdi.gateway.listeners.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RemoteDesktopServer {

    private final SocketIOServer server;
    private final DesktopConnectionService desktopConnectionService;
    private final InstanceService instanceService;
    private final TokenAuthenticatorService authenticator;
    private final InstanceSessionService instanceSessionService;
    private final InstanceActivityService instanceActivityService;
    private final DesktopAccessService desktopAccessService;
    private final VirtualDesktopConfiguration virtualDesktopConfiguration;

    @Inject
    public RemoteDesktopServer(final SocketIOServer server,
                               final InstanceSessionService instanceSessionService,
                               final DesktopConnectionService desktopConnectionService,
                               final InstanceService instanceService,
                               final TokenAuthenticatorService authenticator,
                               final DesktopAccessService desktopAccessService,
                               final VirtualDesktopConfiguration virtualDesktopConfiguration,
                               final InstanceActivityService instanceActivityService) {
        this.server = server;
        this.instanceSessionService = instanceSessionService;
        this.desktopConnectionService = desktopConnectionService;
        this.instanceService = instanceService;
        this.authenticator = authenticator;
        this.desktopAccessService = desktopAccessService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;
        this.instanceActivityService = instanceActivityService;
    }

    public void startServer() {
        if (this.virtualDesktopConfiguration.cleanupSessionsOnStartup()) {
            this.cleanupSessions();
        }

        this.bindListeners(server);
        this.server.start();
    }

    public void stopServer() {
        this.server.stop();
        // make sure the store factory has been shutdown
        this.server.getConfiguration().getStoreFactory().shutdown();
    }

    private void bindListeners(final SocketIOServer server) {
        server.addConnectListener(new ClientConnectListener(this.desktopConnectionService, this.desktopAccessService, this.instanceSessionService, this.authenticator));
        server.addEventListener("display", String.class, new GuacamoleClientDisplayListener(this.desktopConnectionService, this.instanceService, this.instanceSessionService, this.instanceActivityService));
        server.addEventListener("webxdisplay", byte[].class, new WebXClientDisplayListener(this.desktopConnectionService, this.instanceService, this.instanceSessionService, this.instanceActivityService));
        server.addEventListener("thumbnail", byte[].class, new ClientThumbnailListener(this.desktopConnectionService, this.instanceService));
        server.addEventListener(Event.ACCESS_REPLY_EVENT, AccessRequestResponseEvent.class, new ClientAccessReplyListener(this.desktopAccessService));
        server.addEventListener(Event.ACCESS_REVOKED_EVENT, AccessRevokedEvent.class, new ClientAccessRevokedCommandListener(this.desktopConnectionService));
        server.addDisconnectListener(new ClientDisconnectListener(this.desktopConnectionService, this.desktopAccessService, this.instanceSessionService, this.instanceService, this.virtualDesktopConfiguration));
    }

    private void cleanupSessions() {
        this.instanceSessionService.cleanupSession();
    }

}
