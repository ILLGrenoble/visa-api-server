package eu.ill.visa.web.managed;

import jakarta.inject.Inject;
import eu.ill.visa.vdi.VirtualDesktopApplication;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ManagedVirtualDesktopServer implements Managed {

    private final VirtualDesktopApplication application;
    private final VirtualDesktopConfiguration configuration;
    private final Logger logger = LoggerFactory.getLogger(ManagedVirtualDesktopServer.class);

    @Inject
    public ManagedVirtualDesktopServer(final VirtualDesktopApplication application,
                                       final VirtualDesktopConfiguration configuration) {
        this.application = application;
        this.configuration = configuration;
    }

    @Override
    public void start() throws Exception {
        try {

            if (configuration.isEnabled()) {
                logger.info("Starting virtual desktop server");
                this.application.startServer();

            } else {
                logger.info("Virtual desktop server is disabled");
            }

        } catch (Exception e) {
            logger.error("Error creating Virtual Desktop Application: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        if (configuration.isEnabled()) {
            logger.info("Stopping virtual desktop server");
            this.application.stopServer();
        }
    }

}
