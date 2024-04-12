package eu.ill.visa.vdi.concurrency;

import com.corundumstudio.socketio.SocketIOClient;
import jakarta.inject.Singleton;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.webx.WebXTunnel;
import org.apache.guacamole.net.GuacamoleTunnel;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

@Singleton
public class ConnectionThreadExecutor {

    private final ExecutorService executorService = newCachedThreadPool(new ConnectionThreadFactory());

    public ConnectionThread startGuacamoleConnectionThread(SocketIOClient client, GuacamoleTunnel tunnel, Instance instance, User user, Role role) {
        final ConnectionThread thread = new GuacamoleConnectionThread(client, tunnel, instance, user, role);

        executorService.submit(thread);

        return thread;
    }

    public ConnectionThread startWebXConnectionThread(SocketIOClient client, WebXTunnel tunnel, Instance instance, User user, Role role) {
        final ConnectionThread thread = new WebXConnectionThread(client, tunnel, instance, user, role);

        executorService.submit(thread);

        return thread;
    }
}
