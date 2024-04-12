package eu.ill.visa.vdi;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.store.MemoryStoreFactory;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.corundumstudio.socketio.store.StoreFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import jakarta.inject.Singleton;
import eu.ill.visa.vdi.exceptions.DefaultExceptionListener;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualDesktopModule extends AbstractModule {

    private final static Logger logger = LoggerFactory.getLogger(VirtualDesktopModule.class);

    @Override
    protected void configure() {

    }

    @Singleton
    @Provides
    public StoreFactory providesStoreFactory(final VirtualDesktopConfiguration configuration) {
        final boolean redisEnabled = configuration.isRedisEnabled();
        if (redisEnabled) {
            final String redisURL = configuration.getRedisURL();
            final String redisPassword = configuration.getRedisPassword();
            final Integer redisDatabase = configuration.getRedisDatabase();
            logger.info("Enabling load-balanced web-sockets with redis at {}, using db {}", redisURL, redisDatabase);
            final Config config = new Config();
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

    @Singleton
    @Provides
    public Configuration providesConfiguration(final VirtualDesktopConfiguration configuration, final StoreFactory storeFactory) {
        final String host = configuration.getHost();
        final Integer port = configuration.getPort();
        final Configuration config = new Configuration();
        config.setHostname(host);
        config.setPort(port);
        config.setWebsocketCompression(true);
        config.setOrigin(configuration.getCorsOrigin());
        config.setRandomSession(true);
        config.setStoreFactory(storeFactory);
        config.setExceptionListener(new DefaultExceptionListener());
        config.setPingInterval(configuration.getPingInterval());
        config.setPingTimeout(configuration.getPingTimeout());
        // set the max frame payload length to 200kb
        config.setMaxFramePayloadLength(200000);
        SocketConfig socketConfig = config.getSocketConfig();
        socketConfig.setReuseAddress(true);
        return config;
    }

    @Provides
    @Singleton
    SocketIOServer providesSocketIOServer(final Configuration configuration) {
        return new SocketIOServer(configuration);
    }

}
