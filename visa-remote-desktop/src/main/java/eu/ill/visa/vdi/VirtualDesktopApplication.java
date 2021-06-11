package eu.ill.visa.vdi;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
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
