package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.ClientEventDispatcher;
import eu.ill.visa.vdi.gateway.events.ClientEventCarrier;

public class ClientEventCarrierListener implements DataListener<ClientEventCarrier> {

    private final ClientEventDispatcher clientEventDispatcher;

    public ClientEventCarrierListener(final ClientEventDispatcher clientEventDispatcher) {
        this.clientEventDispatcher = clientEventDispatcher;
    }

    @Override
    public void onData(final SocketIOClient client, final ClientEventCarrier data, final AckRequest ackRequest) {
        final SocketClient socketClient = new SocketClient(client, client.getSessionId().toString());
        this.clientEventDispatcher.onEvent(socketClient, data);
    }
}

