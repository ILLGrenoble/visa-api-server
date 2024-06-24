package eu.ill.visa.vdi;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import eu.ill.visa.business.services.ImageProtocolService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.business.services.SignatureService;
import eu.ill.visa.vdi.business.concurrency.ConnectionThreadExecutor;
import eu.ill.visa.vdi.business.services.GuacamoleDesktopService;
import eu.ill.visa.vdi.business.services.WebXDesktopService;
import eu.ill.visa.vdi.domain.exceptions.DefaultExceptionListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class VirtualDesktopProducer {

    private final static Logger logger = LoggerFactory.getLogger(VirtualDesktopProducer.class);

    private final VirtualDesktopConfiguration configuration;
    private final InstanceSessionService instanceSessionService;
    private final SignatureService signatureService;
    private final ImageProtocolService imageProtocolService;
    private final ConnectionThreadExecutor executorService;

    @Inject
    public VirtualDesktopProducer(final VirtualDesktopConfiguration configuration,
                                  final InstanceSessionService instanceSessionService,
                                  final SignatureService signatureService,
                                  final ImageProtocolService imageProtocolService,
                                  final ConnectionThreadExecutor executorService) {
        this.configuration = configuration;
        this.instanceSessionService = instanceSessionService;
        this.signatureService = signatureService;
        this.imageProtocolService = imageProtocolService;
        this.executorService = executorService;
    }

    private Configuration configuration() {
        final String host = configuration.host();
        final Integer port = configuration.port();
        final Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setWebsocketCompression(true);
        config.setOrigin(configuration.corsOrigin());
        config.setRandomSession(true);
        config.setExceptionListener(new DefaultExceptionListener());
        config.setPingInterval(configuration.pingInterval());
        config.setPingTimeout(configuration.pingTimeout());
        // set the max frame payload length to 200kb
        config.setMaxFramePayloadLength(200000);
        SocketConfig socketConfig = config.getSocketConfig();
        socketConfig.setReuseAddress(true);
        return config;
    }

    @Produces
    public SocketIOServer socketIOServer() {
        return new SocketIOServer(configuration());
    }

    @Produces
    public WebXDesktopService webXDesktopService() {
        return new WebXDesktopService(instanceSessionService, signatureService, imageProtocolService, executorService);
    }

    @Produces
    public GuacamoleDesktopService guacamoleDesktopService() {
        return new GuacamoleDesktopService(instanceSessionService, signatureService, imageProtocolService, configuration, executorService);
    }

}
