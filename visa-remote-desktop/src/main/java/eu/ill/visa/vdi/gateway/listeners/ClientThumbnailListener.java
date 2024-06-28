package eu.ill.visa.vdi.gateway.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClientThumbnailListener implements DataListener<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(ClientThumbnailListener.class);

    private final DesktopSessionService desktopSessionService;
    private final InstanceService instanceService;

    public ClientThumbnailListener(final DesktopSessionService desktopSessionService,
                                   final InstanceService instanceService) {
        this.desktopSessionService = desktopSessionService;
        this.instanceService = instanceService;
    }

    @Override
    public void onData(final SocketIOClient client, final byte[] data, final AckRequest ackRequest) {
        final SocketClient socketClient = new SocketClient(client, client.getSessionId().toString());
        this.desktopSessionService.findDesktopSessionMember(socketClient).ifPresent(desktopSessionMember -> {
            try {
                if (desktopSessionMember.getConnectedUser().hasAnyRole(List.of(InstanceMemberRole.OWNER, InstanceMemberRole.SUPPORT))) {
                    final Instance instance = instanceService.getById(desktopSessionMember.getSession().getInstanceId());
                    if (instance != null) {
                        final ImageFormat mimeType = Imaging.guessFormat(data);
                        if (mimeType == ImageFormats.JPEG) {
                            instanceService.createOrUpdateThumbnail(instance, data);
                        }
                    }
                }
            } catch (Exception exception) {
                logger.error("Error creating thumbnail for instance: {}", desktopSessionMember.getSession().getInstanceId(), exception);
            }
        });
    }
}
