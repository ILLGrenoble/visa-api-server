package eu.ill.visa.vdi.concurrency;

import com.corundumstudio.socketio.SocketIOClient;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.vdi.domain.Role;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

@Singleton
public class ConnectionThreadExecutor {

    private final ExecutorService executorService = newCachedThreadPool(new ConnectionThreadFactory());

    public ConnectionThread startConnectionThread(SocketIOClient client, GuacamoleSocket socket, Instance instance, User user, Role role) {
        final GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);
        final ConnectionThread thread = new ConnectionThread(client, tunnel, instance, user, role);

        executorService.submit(thread);

        return thread;
    }
}
