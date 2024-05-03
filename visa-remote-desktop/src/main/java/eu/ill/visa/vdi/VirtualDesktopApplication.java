package eu.ill.visa.vdi;

import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class VirtualDesktopApplication {

    private static final Logger logger = LoggerFactory.getLogger(VirtualDesktopApplication.class);

    private final RemoteDesktopServer remoteDesktopServer;
    private final VirtualDesktopConfiguration configuration;

    @Inject
    public VirtualDesktopApplication(final RemoteDesktopServer remoteDesktopServer,
                                     final VirtualDesktopConfiguration configuration) {
        this.remoteDesktopServer = remoteDesktopServer;
        this.configuration = configuration;
    }

    @Startup
    public void startServer() {
        try {
            if (configuration.enabled()) {
                logger.info("Starting Virtual Desktop Server");
                this.remoteDesktopServer.startServer();

            } else {
                logger.info("Virtual Desktop Server is disabled");
            }

        } catch (Exception e) {
            logger.error("Error creating Virtual Desktop Server: {}", e.getMessage());
            throw e;
        }
    }

    @Shutdown
    public void stopServer() {
        if (configuration.enabled()) {
            logger.info("Stopping Virtual Desktop Server");
            this.remoteDesktopServer.stopServer();
        }
    }

}
