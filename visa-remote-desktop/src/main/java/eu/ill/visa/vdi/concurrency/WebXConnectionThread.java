package eu.ill.visa.vdi.concurrency;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.webx.WebXTunnel;
import eu.ill.webx.exceptions.WebXClientException;
import eu.ill.webx.exceptions.WebXConnectionInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class WebXConnectionThread extends ConnectionThread {

    private static final Logger logger = LoggerFactory.getLogger(WebXConnectionThread.class);
    private final WebXTunnel tunnel;

    public WebXConnectionThread(final SocketIOClient client, final WebXTunnel tunnel, final Instance instance, final User user, final Role role) {
        super(client, instance, user, role);
        this.tunnel = tunnel;
    }

    @Override
    public void closeTunnel() {
        this.tunnel.disconnect();
    }

    @Override
    public void run() {
        read();
    }

    @Override
    public void writeCharData(char[] data) {
        logger.error("writeCharData Not implemented");
    }

    @Override
    public void writeByteData(byte[] data) {
        try {
            this.tunnel.write(data);

        } catch (WebXClientException exception) {
            logger.debug("Connection to webx server is closed", exception);
        }
    }

    private void read() {

        try {
            this.tunnel.start();

            byte[] messageData;
            while (tunnel.isRunning() && (messageData = tunnel.read()) != null) {
                client.sendEvent("display", ByteBuffer.wrap(messageData));
            }

        } catch (WebXClientException exception) {
            logger.error("WebSocket connection terminated due to client error {}", exception.getMessage());

        } catch (WebXConnectionInterruptException exception) {
            logger.error("WebSocket connection terminated due to interruption {}", exception.getMessage());
        }

        client.disconnect();
    }
}

