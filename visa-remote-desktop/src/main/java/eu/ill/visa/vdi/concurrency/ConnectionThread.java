package eu.ill.visa.vdi.concurrency;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.vdi.domain.Role;
import org.apache.guacamole.GuacamoleClientException;
import org.apache.guacamole.GuacamoleConnectionClosedException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.protocol.GuacamoleInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.guacamole.net.GuacamoleTunnel.INTERNAL_DATA_OPCODE;

public class ConnectionThread implements Runnable {

    private static final Logger          logger = LoggerFactory.getLogger(ConnectionThread.class);
    private final        SocketIOClient  client;
    private final        GuacamoleTunnel tunnel;
    private final Instance instance;
    private final User user;
    private final Role role;

    public ConnectionThread(final SocketIOClient client, final GuacamoleTunnel tunnel, final Instance instance, final User user, final Role role) {
        this.client = client;
        this.tunnel = tunnel;
        this.instance = instance;
        this.user = user;
        this.role = role;
    }

    public GuacamoleTunnel getTunnel() {
        return tunnel;
    }

    public void closeTunnel() {
        try {
            if (this.tunnel != null) {
                this.tunnel.close();
            }

        } catch (GuacamoleException exception) {
            logger.debug("Unable to close connection to guacd for {} : {}", this.getInstanceAndUser(), exception.getMessage());
        }
    }

    @Override
    public void run() {
        sendIdentifierInstruction();
        read();
    }

    private void read() {
        final StringBuilder   buffer = new StringBuilder(8192);
        final GuacamoleReader reader = tunnel.acquireReader();
        char[]                readMessage;

        try {
            while ((readMessage = reader.read()) != null) {

                buffer.append(readMessage);

                // Flush if we expect to wait or buffer is getting full
                if (!reader.available() || buffer.length() >= 8192) {
                    client.sendEvent("display", buffer.toString());
                    buffer.setLength(0);
                }

            }
            client.disconnect();
        } catch (GuacamoleClientException exception) {
            logger.error("WebSocket connection terminated for {} due to client error {}", this.getInstanceAndUser(), exception.getMessage());
            client.disconnect();
        } catch (GuacamoleConnectionClosedException exception) {
            logger.info("Connection to guacd closed for {} : {}", this.getInstanceAndUser(), exception.getMessage());
            client.disconnect();
        } catch (GuacamoleException exception) {
            logger.error("Connection to guacd for {} terminated abnormally: {}", this.getInstanceAndUser(), exception.getMessage());
            client.disconnect();
        }
    }

    private void sendIdentifierInstruction() {
        final String               uuid        = tunnel.getUUID().toString();
        final GuacamoleInstruction instruction = new GuacamoleInstruction(INTERNAL_DATA_OPCODE, uuid);

        client.sendEvent("display", instruction.toString());
    }

    private String getInstanceAndUser() {
        return "User " + this.user.getFullName() + " (" + this.user.getId() + ", " + this.role.toString() + "), Instance " + this.instance.getId() + ", Session " + this.client.getSessionId();
    }
}

