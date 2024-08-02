package eu.ill.visa.business.services;

import eu.ill.visa.business.BusinessConfiguration;
import eu.ill.visa.core.entity.ImageProtocol;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PortService {

    private final int portCheckTimeoutMs;

    public PortService(final BusinessConfiguration configuration) {
        this.portCheckTimeoutMs =configuration.instance().portCheckTimeoutMs();
    }

    public boolean areMandatoryPortsOpen(String hostname, List<ImageProtocol> protocols) {
        if (!protocols.isEmpty()) {
            for (ImageProtocol protocol : protocols) {
                if (!protocol.isOptional() && !isPortOpen(hostname, protocol.getPort())) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public List<ImageProtocol> getActiveProtocols(String hostname, List<ImageProtocol> protocols) {
        List<ImageProtocol> activeProtocols = new ArrayList<>();
        if (!protocols.isEmpty()) {
            for (ImageProtocol protocol : protocols) {
                if (isPortOpen(hostname, protocol.getPort())) {
                    activeProtocols.add(protocol);
                }
            }
        }
        return activeProtocols;
    }

    /**
     * Check if a port is open for a given hostname
     */
    public boolean isPortOpen(String hostname, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, port), this.portCheckTimeoutMs);
            return true;
        } catch (IOException exception) {
            return false;
        }

    }
}
