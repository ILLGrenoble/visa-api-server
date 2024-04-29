package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.ImageProtocol;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PortService {

    public record Service(String hostname, int port) {
    }

    public static boolean areMandatoryPortsOpen(String hostname, List<ImageProtocol> protocols) {
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

    public static List<ImageProtocol> getActiveProtocols(String hostname, List<ImageProtocol> protocols) {
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
