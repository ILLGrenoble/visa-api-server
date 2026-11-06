package eu.ill.visa.vdi.business.concurrency;

import eu.ill.visa.common.vdi.domain.VdiInstance;
import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.vdi.domain.models.PingResponseHandler;
import eu.ill.visa.vdi.domain.models.SocketClient;

public abstract class ConnectionThread implements Runnable {

    protected final SocketClient client;
    private final VdiInstance instance;
    private final ConnectedUser user;

    public ConnectionThread(final SocketClient client, final VdiInstance instance, final ConnectedUser user) {
        this.client = client;
        this.instance = instance;
        this.user = user;
    }

    public abstract void closeTunnel();

    public abstract void run();
    public abstract void setPingResponseHandler(PingResponseHandler pingResponseHandler);

    public abstract void writeCharData(char[] data);
    public abstract void writeByteData(byte[] data);

    protected String getInstanceAndUser() {
        return "User " + this.user.fullName() + " (" + this.user.id() + ", " + this.user.role().toString() + "), Instance " + this.instance.id() + ", Client Id " + this.client.clientId();
    }
}

