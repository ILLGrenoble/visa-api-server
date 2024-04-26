package eu.ill.visa.vdi;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VirtualDesktopApplication {

    private final RemoteDesktopServer remoteDesktopServer;

    @Inject
    public VirtualDesktopApplication(final RemoteDesktopServer remoteDesktopServer) {
        this.remoteDesktopServer = remoteDesktopServer;
    }

    public void startServer() {
        this.remoteDesktopServer.startServer();
    }

    public void stopServer() {
        this.remoteDesktopServer.stopServer();
    }
}
