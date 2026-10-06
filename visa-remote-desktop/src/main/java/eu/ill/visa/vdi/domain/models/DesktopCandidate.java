package eu.ill.visa.vdi.domain.models;

public record DesktopCandidate(SocketClient client,
                               String connectionId,
                               ConnectedUser connectedUser,
                               Long instanceId,
                               NopSender nopSender) {

    public void keepAlive() {
        this.nopSender.sendNop(client);
    }
}
