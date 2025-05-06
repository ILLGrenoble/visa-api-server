package eu.ill.visa.vdi.business.concurrency;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.vdi.domain.models.ConnectedUser;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.webx.WebXTunnel;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.guacamole.net.GuacamoleTunnel;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

@ApplicationScoped
public class ConnectionThreadExecutor {

    private final ExecutorService executorService = newCachedThreadPool(new ConnectionThreadFactory());

    public ConnectionThread createGuacamoleConnectionThread(SocketClient client, GuacamoleTunnel tunnel, Instance instance, ConnectedUser user) {
        return new GuacamoleConnectionThread(client, tunnel, instance, user);
    }

    public ConnectionThread createWebXConnectionThread(SocketClient client, WebXTunnel tunnel, Instance instance, ConnectedUser user) {
        return new WebXConnectionThread(client, tunnel, instance, user);
    }

    public void startConnectionThread(final ConnectionThread thread) {
        executorService.submit(thread);
    }
}
