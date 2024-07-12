package eu.ill.visa.vdi.domain.models;

public record DesktopCandidate(SocketClient client, Long sessionId, PendingDesktopSessionMember pendingDesktopSessionMember,
                               NopSender nopSender) {

    public void keepAlive() {
        this.nopSender.sendNop(client);
    }
}
