package eu.ill.visa.vdi.domain.models;

import com.corundumstudio.socketio.SocketIOClient;

public record SocketClient(SocketIOClient socketIOClient, String connectionId) {
    public <T> void sendEvent(String type) {
        this.socketIOClient.sendEvent(type);
    }

    public <T> void sendEvent(String type, T event) {
        this.socketIOClient.sendEvent(type, event);
    }

    public void disconnect() {
        this.socketIOClient.disconnect();
    }

    public boolean isChannelOpen() {
        return this.socketIOClient.isChannelOpen();
    }
}
