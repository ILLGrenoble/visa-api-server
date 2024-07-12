package eu.ill.visa.vdi.domain.models;

public record DesktopCandidate(SocketClient client, Long sessionId, PendingDesktopSessionMember pendingDesktopSessionMember) {

    public void keepAlive() {
        this.client.sendEvent("3.nop;");
    }
}
