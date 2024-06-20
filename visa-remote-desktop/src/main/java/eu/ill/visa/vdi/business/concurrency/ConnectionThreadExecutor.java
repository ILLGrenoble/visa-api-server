package eu.ill.visa.vdi.business.concurrency;

import com.corundumstudio.socketio.SocketIOClient;
import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.vdi.domain.models.Role;
import eu.ill.webx.WebXTunnel;
import org.apache.guacamole.net.GuacamoleTunnel;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

@ApplicationScoped
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
