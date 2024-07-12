package eu.ill.visa.vdi.business.concurrency;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.webx.WebXTunnel;
import eu.ill.webx.exceptions.WebXClientException;
import eu.ill.webx.exceptions.WebXConnectionInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebXConnectionThread extends ConnectionThread {

    private static final Logger logger = LoggerFactory.getLogger(WebXConnectionThread.class);
    private final WebXTunnel tunnel;

    public WebXConnectionThread(final SocketClient client, final WebXTunnel tunnel, final Instance instance, final ConnectedUser user) {
        super(client, instance, user);
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
                client.sendEvent(messageData);
            }

        } catch (WebXClientException exception) {
            logger.error("WebSocket connection terminated due to client error {}", exception.getMessage());

        } catch (WebXConnectionInterruptException exception) {
            logger.error("WebSocket connection terminated due to interruption {}", exception.getMessage());
        }

        client.disconnect();
    }
}

