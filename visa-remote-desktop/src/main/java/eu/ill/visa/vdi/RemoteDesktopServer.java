package eu.ill.visa.vdi;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.corundumstudio.socketio.store.StoreFactory;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubType;
import eu.ill.visa.business.services.InstanceActivityService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopConnectionService;
import eu.ill.visa.vdi.business.services.RoleService;
import eu.ill.visa.vdi.business.services.TokenAuthenticatorService;
import eu.ill.visa.vdi.domain.events.Event;
import eu.ill.visa.vdi.gateway.events.AccessRequestReply;
import eu.ill.visa.vdi.gateway.events.AccessRevokedCommand;
import eu.ill.visa.vdi.gateway.listeners.*;
import eu.ill.visa.vdi.gateway.pubsub.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RemoteDesktopServer {

    private final static Logger logger = LoggerFactory.getLogger(RemoteDesktopServer.class);

    private final SocketIOServer server;
    private final DesktopConnectionService desktopConnectionService;
    private final InstanceService instanceService;
    private final TokenAuthenticatorService authenticator;
    private final RoleService roleService;
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
                               final RoleService roleService,
                               final DesktopAccessService desktopAccessService,
                               final VirtualDesktopConfiguration virtualDesktopConfiguration,
                               final InstanceActivityService instanceActivityService) {
        this.server = server;
        this.instanceSessionService = instanceSessionService;
        this.desktopConnectionService = desktopConnectionService;
        this.instanceService = instanceService;
        this.authenticator = authenticator;
        this.roleService = roleService;
        this.desktopAccessService = desktopAccessService;
        this.virtualDesktopConfiguration = virtualDesktopConfiguration;
        this.instanceActivityService = instanceActivityService;
    }

    public void startServer() {
        if (this.virtualDesktopConfiguration.cleanupSessionsOnStartup()) {
            this.cleanupSessions();
        }

        this.bindListeners(server);
        this.bindStoreFactorySubscriptions();
        this.server.start();
    }

    public void stopServer() {
        this.server.stop();
        // make sure the store factory has been shutdown
        this.server.getConfiguration().getStoreFactory().shutdown();
    }

    private void bindListeners(final SocketIOServer server) {
        server.addConnectListener(new ClientConnectListener(this.desktopConnectionService, this.desktopAccessService, this.instanceSessionService, this.roleService, this.authenticator));
        server.addEventListener("display", String.class, new GuacamoleClientDisplayListener(this.desktopConnectionService, this.instanceService, this.instanceSessionService, this.instanceActivityService));
        server.addEventListener("webxdisplay", byte[].class, new WebXClientDisplayListener(this.desktopConnectionService, this.instanceService, this.instanceSessionService, this.instanceActivityService));
        server.addEventListener("thumbnail", byte[].class, new ClientThumbnailListener(this.desktopConnectionService, this.instanceService));
        server.addEventListener(Event.ACCESS_REPLY_EVENT, AccessRequestReply.class, new ClientAccessReplyListener(this.desktopAccessService));
        server.addEventListener(Event.ACCESS_REVOKED_EVENT, AccessRevokedCommand.class, new ClientAccessRevokedCommandListener(this.desktopConnectionService, this.instanceSessionService, this.instanceService));
        server.addDisconnectListener(new ClientDisconnectListener(this.desktopConnectionService, this.desktopAccessService, this.instanceSessionService, this.instanceService, this.virtualDesktopConfiguration));
    }

    private void bindStoreFactorySubscriptions() {
        final StoreFactory storeFactory = this.server.getConfiguration().getStoreFactory();
        if (storeFactory instanceof RedissonStoreFactory) {
            logger.info("Binding pub-sub store subscriptions");
            storeFactory.pubSubStore().subscribe(PubSubType.DISPATCH, new ServerRoomClosedListener(this.server), DispatchMessage.class);
            storeFactory.pubSubStore().subscribe(PubSubType.DISPATCH, new ServerRoomLockedListener(this.desktopConnectionService, this.server), DispatchMessage.class);
            storeFactory.pubSubStore().subscribe(PubSubType.DISPATCH, new ServerRoomUnlockedListener(this.desktopConnectionService, this.server), DispatchMessage.class);
            storeFactory.pubSubStore().subscribe(PubSubType.DISPATCH, new ServerAccessRevokedListener(this.server), DispatchMessage.class);
        }
    }

    private void cleanupSessions() {
        this.instanceSessionService.cleanupSession();
    }

}
