package eu.ill.visa.vdi;

import eu.ill.visa.broker.EventDispatcher;
import eu.ill.visa.broker.gateway.ClientEventsGateway;
import eu.ill.visa.business.services.InstanceActivityService;
import eu.ill.visa.business.services.InstanceAuthenticationTokenService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.vdi.business.services.DesktopAccessService;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.SessionEvent;
import eu.ill.visa.vdi.gateway.events.AccessRequestResponseEvent;
import eu.ill.visa.vdi.gateway.events.AccessRevokedEvent;
import eu.ill.visa.vdi.display.sockets.GuacamoleRemoteDesktopSocket;
import eu.ill.visa.vdi.display.sockets.WebXRemoteDesktopSocket;
import eu.ill.visa.vdi.display.subscribers.GuacamoleRemoteDesktopEventSubscriber;
import eu.ill.visa.vdi.display.subscribers.RemoteDesktopConnectSubscriber;
import eu.ill.visa.vdi.display.subscribers.RemoteDesktopDisconnectSubscriber;
import eu.ill.visa.vdi.display.subscribers.WebXRemoteDesktopEventSubscriber;
import eu.ill.visa.vdi.gateway.subscribers.EventChannelAccessRequestResponseSubscriber;
import eu.ill.visa.vdi.gateway.subscribers.AccessRevokedSubscriber;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RemoteDesktopServer {

    private static final Logger logger = LoggerFactory.getLogger(RemoteDesktopServer.class);

    private final DesktopSessionService desktopConnectionService;
    private final InstanceService instanceService;
    private final InstanceAuthenticationTokenService authenticator;
    private final InstanceSessionService instanceSessionService;
    private final InstanceActivityService instanceActivityService;
    private final DesktopAccessService desktopAccessService;
    private final VirtualDesktopConfiguration configuration;
    private final ClientEventsGateway clientEventsGateway;
    private final EventDispatcher eventDispatcher;
    private final GuacamoleRemoteDesktopSocket guacamoleRemoteDesktopSocket;
    private final WebXRemoteDesktopSocket webXRemoteDesktopSocket;

    @Inject
    public RemoteDesktopServer(final InstanceSessionService instanceSessionService,
                               final DesktopSessionService desktopConnectionService,
                               final InstanceService instanceService,
                               final InstanceAuthenticationTokenService authenticator,
                               final DesktopAccessService desktopAccessService,
                               final VirtualDesktopConfiguration configuration,
                               final InstanceActivityService instanceActivityService,
                               final ClientEventsGateway clientEventsGateway,
                               final EventDispatcher eventDispatcher,
                               final GuacamoleRemoteDesktopSocket guacamoleRemoteDesktopSocket,
                               final WebXRemoteDesktopSocket webXRemoteDesktopSocket) {
        this.instanceSessionService = instanceSessionService;
        this.desktopConnectionService = desktopConnectionService;
        this.instanceService = instanceService;
        this.authenticator = authenticator;
        this.desktopAccessService = desktopAccessService;
        this.configuration = configuration;
        this.instanceActivityService = instanceActivityService;
        this.clientEventsGateway = clientEventsGateway;
        this.eventDispatcher = eventDispatcher;
        this.guacamoleRemoteDesktopSocket = guacamoleRemoteDesktopSocket;
        this.webXRemoteDesktopSocket = webXRemoteDesktopSocket;
    }

    @Startup
    public void startServer() {
        if (configuration.enabled()) {
            logger.info("Virtual Desktop Server is enabled");
            if (this.configuration.cleanupSessionsOnStartup()) {
                this.cleanupSessions();
            }

            this.bindSubscribers();

        } else {
            logger.info("Virtual Desktop Server is disabled");
        }
    }

    private void bindSubscribers() {
        // Set up event channel
        this.clientEventsGateway.subscribe(SessionEvent.ACCESS_REPLY_EVENT, AccessRequestResponseEvent.class)
            .next(new EventChannelAccessRequestResponseSubscriber(this.desktopAccessService, this.desktopConnectionService));
        this.clientEventsGateway.subscribe(SessionEvent.ACCESS_REVOKED_EVENT, AccessRevokedEvent.class)
            .next(new AccessRevokedSubscriber(this.desktopConnectionService));

        // Set up guacamole display listeners
        this.guacamoleRemoteDesktopSocket.setConnectSubscriber(new RemoteDesktopConnectSubscriber(this.desktopConnectionService, this.desktopAccessService, this.instanceService, this.instanceSessionService, this.authenticator, this.eventDispatcher));
        this.guacamoleRemoteDesktopSocket.setDisconnectSubscriber(new RemoteDesktopDisconnectSubscriber(this.desktopConnectionService, this.desktopAccessService));
        this.guacamoleRemoteDesktopSocket.setEventSubscriber(new GuacamoleRemoteDesktopEventSubscriber(this.desktopConnectionService, this.instanceService, this.instanceSessionService, this.instanceActivityService));

        // Set up webx display listeners
        this.webXRemoteDesktopSocket.setConnectSubscriber(new RemoteDesktopConnectSubscriber(this.desktopConnectionService, this.desktopAccessService, this.instanceService, this.instanceSessionService, this.authenticator, this.eventDispatcher));
        this.webXRemoteDesktopSocket.setDisconnectSubscriber(new RemoteDesktopDisconnectSubscriber(this.desktopConnectionService, this.desktopAccessService));
        this.webXRemoteDesktopSocket.setEventSubscriber(new WebXRemoteDesktopEventSubscriber(this.desktopConnectionService, this.instanceService, this.instanceSessionService, this.instanceActivityService));
    }

    private void cleanupSessions() {
        this.instanceSessionService.cleanupSession();
    }

}
