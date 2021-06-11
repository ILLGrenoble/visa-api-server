package eu.ill.visa.business.services;

import eu.ill.visa.core.domain.ImageProtocol;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class PortService {

    public static class Service {
        private final String hostname;
        private final int port;

        public Service(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
        }

        public String getHostname() {
            return hostname;
        }

        public int getPort() {
            return port;
        }
    }

    public static boolean arePortsOpen(String hostname, List<ImageProtocol> protocols) {
        if (protocols.size() > 0) {
            for (ImageProtocol protocol : protocols) {
                if (!isPortOpen(hostname, protocol.getPort())) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Check if a port is open for a given hostname
     */
    public static boolean isPortOpen(String hostname, int port) {
        try (Socket ignored = new Socket(hostname, port)) {
            return true;
        } catch (IOException exception) {
            return false;
        }

    }

    public static void main(String[] args) {
        if (isPortOpen("localhost", 3389)) {
            System.out.println("Yes");
        } else {
            System.out.println("No");
        }
    }

    public static Service createService(String hostname, int port) {
        return new Service(hostname, port);
    }
}
