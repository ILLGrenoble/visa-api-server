package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.vdi.gateway.events.ClientEventCarrier;

public record EventChannel(SocketClient client) {

    public void sendEvent(String type) {
        this.sendEvent(type, null);
    }

    public <T> void sendEvent(String type, T data) {
        this.client.sendEvent(new ClientEventCarrier(type, data));
    }

    public void disconnect() {
        this.client.disconnect();
    }
}
