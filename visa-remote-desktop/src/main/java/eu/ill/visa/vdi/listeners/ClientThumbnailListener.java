package eu.ill.visa.vdi.listeners;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.vdi.domain.Role;
import eu.ill.visa.vdi.models.DesktopConnection;
import eu.ill.visa.vdi.services.DesktopConnectionService;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ClientThumbnailListener extends AbstractListener implements DataListener<byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(ClientThumbnailListener.class);

    private final InstanceService instanceService;

    public ClientThumbnailListener(final DesktopConnectionService desktopConnectionService,
                                   final InstanceService instanceService) {
        super(desktopConnectionService);
        this.instanceService = instanceService;
    }

    @Override
    public void onData(final SocketIOClient client, final byte[] data, final AckRequest ackRequest) {
        final DesktopConnection connection = this.getDesktopConnection(client);
        try {
            if (connection == null) {
                return;
            }
            if (connection.getConnectedUser().hasAnyRole(List.of(Role.OWNER, Role.SUPPORT))) {
                final Instance instance = instanceService.getById(connection.getInstanceId());
                if (instance != null) {
                    final ImageFormat mimeType = Imaging.guessFormat(data);
                    if (mimeType == ImageFormats.JPEG) {
                        instanceService.createOrUpdateThumbnail(instance, data);
                    }
                }
            }
        } catch (IOException | ImageReadException exception) {
            logger.error("Error creating thumbnail for instance: {}", connection.getInstanceId(), exception);
        }


    }
}
