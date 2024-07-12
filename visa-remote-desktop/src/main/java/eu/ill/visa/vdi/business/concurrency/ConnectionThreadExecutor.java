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

    public ConnectionThread startGuacamoleConnectionThread(SocketClient client, GuacamoleTunnel tunnel, Instance instance, ConnectedUser user) {
        final ConnectionThread thread = new GuacamoleConnectionThread(client, tunnel, instance, user);

        executorService.submit(thread);

        return thread;
    }

    public ConnectionThread startWebXConnectionThread(SocketClient client, WebXTunnel tunnel, Instance instance, ConnectedUser user) {
        final ConnectionThread thread = new WebXConnectionThread(client, tunnel, instance, user);

        executorService.submit(thread);

        return thread;
    }
}
