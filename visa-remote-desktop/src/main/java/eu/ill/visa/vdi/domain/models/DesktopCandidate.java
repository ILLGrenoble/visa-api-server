package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.common.vdi.domain.VdiInstance;

public record DesktopCandidate(SocketClient client,
                               String connectionId,
                               ConnectedUser connectedUser,
                               VdiInstance instance,
                               NopSender nopSender) {

    public void keepAlive() {
        this.nopSender.sendNop(client);
    }
}
