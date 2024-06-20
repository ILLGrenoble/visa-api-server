package eu.ill.visa.vdi.business.concurrency;

import com.corundumstudio.socketio.SocketIOClient;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConnectionThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionThread.class);

    protected final SocketIOClient  client;
    private final Instance instance;
    private final ConnectedUser user;

    public ConnectionThread(final SocketIOClient client, final Instance instance, final ConnectedUser user) {
        this.client = client;
        this.instance = instance;
        this.user = user;
    }

    public abstract void closeTunnel();

    public abstract void run();

    public abstract void writeCharData(char[] data);
    public abstract void writeByteData(byte[] data);

    protected String getInstanceAndUser() {
        return "User " + this.user.getFullName() + " (" + this.user.getId() + ", " + this.user.getRole().toString() + "), Instance " + this.instance.getId() + ", Session " + this.client.getSessionId();
    }
}

