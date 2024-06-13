package eu.ill.visa.vdi;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.store.MemoryStoreFactory;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.corundumstudio.socketio.store.StoreFactory;
import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.business.services.ImageProtocolService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.business.services.SignatureService;
import eu.ill.visa.vdi.concurrency.ConnectionThreadExecutor;
import eu.ill.visa.vdi.exceptions.DefaultExceptionListener;
import eu.ill.visa.vdi.services.GuacamoleDesktopService;
import eu.ill.visa.vdi.services.WebXDesktopService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.redisson.Redisson;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class VirtualDesktopProducer {

    private final static Logger logger = LoggerFactory.getLogger(VirtualDesktopProducer.class);

    private final VirtualDesktopConfiguration configuration;
    private final CloudClientService cloudClientService;
    private final InstanceSessionService instanceSessionService;
    private final SignatureService signatureService;
    private final ImageProtocolService imageProtocolService;
    private final ConnectionThreadExecutor executorService;

    @Inject
    public VirtualDesktopProducer(final VirtualDesktopConfiguration configuration,
                                  final CloudClientService cloudClientService,
                                  final InstanceSessionService instanceSessionService,
                                  final SignatureService signatureService,
                                  final ImageProtocolService imageProtocolService,
                                  final ConnectionThreadExecutor executorService) {
        this.configuration = configuration;
        this.cloudClientService = cloudClientService;
        this.instanceSessionService = instanceSessionService;
        this.signatureService = signatureService;
        this.imageProtocolService = imageProtocolService;
        this.executorService = executorService;
    }

    private StoreFactory storeFactory() {
        final boolean redisEnabled = configuration.redisEnabled();
        if (redisEnabled) {
            final String redisURL = configuration.redisURL().orElse(null);
            final String redisPassword = configuration.redisPassword().orElse(null);
            final Integer redisDatabase = configuration.redisDatabase();
            logger.info("Enabling load-balanced web-sockets with redis at {}, using db {}", redisURL, redisDatabase);
            final Config config = new Config();
            config.setCodec(new JsonJacksonCodec());
            config
                .useSingleServer()
                .setAddress(redisURL)
                .setPassword(redisPassword)
                .setDatabase(redisDatabase);
            final Redisson redisson = (Redisson) Redisson.create(config);
            return new RedissonStoreFactory(redisson);
        } else {
            logger.info("Enabling single server web-sockets");
            return new MemoryStoreFactory();
        }
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
        config.setStoreFactory(this.storeFactory());
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
        return new WebXDesktopService(instanceSessionService, cloudClientService, signatureService, imageProtocolService, executorService);
    }

    @Produces
    public GuacamoleDesktopService guacamoleDesktopService() {
        return new GuacamoleDesktopService(instanceSessionService, cloudClientService, signatureService, imageProtocolService, configuration, executorService);
    }

}
