package eu.ill.visa.vdi.gateway.subscribers.events;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import eu.ill.visa.vdi.domain.models.SocketClient;
import eu.ill.visa.vdi.gateway.dispatcher.SocketEventSubscriber;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class EventChannelThumbnailSubscriber implements SocketEventSubscriber<String> {

    private static final Logger logger = LoggerFactory.getLogger(EventChannelThumbnailSubscriber.class);

    private final DesktopSessionService desktopSessionService;
    private final InstanceService instanceService;

    public EventChannelThumbnailSubscriber(final DesktopSessionService desktopSessionService,
                                           final InstanceService instanceService) {
        this.desktopSessionService = desktopSessionService;
        this.instanceService = instanceService;
    }

    @Override
    public void onEvent(final SocketClient client, final String base64) {
        this.desktopSessionService.findDesktopSessionMemberByToken(client.token()).ifPresent(desktopSessionMember -> {
            try {
                if (desktopSessionMember.getConnectedUser().hasAnyRole(List.of(InstanceMemberRole.OWNER, InstanceMemberRole.SUPPORT))) {
                    final Instance instance = instanceService.getById(desktopSessionMember.getSession().getInstanceId());
                    if (instance != null) {
                        final byte[] data = Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
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
