package eu.ill.visa.vdi.display.subscribers;

import eu.ill.visa.business.services.InstanceActivityService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.business.services.DesktopSessionService;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class WebXRemoteDesktopEventSubscriber extends RemoteDesktopEventSubscriber<byte[]> {

    public WebXRemoteDesktopEventSubscriber(final DesktopSessionService desktopSessionService,
                                            final InstanceService instanceService,
                                            final InstanceSessionService instanceSessionService,
                                            final InstanceActivityService instanceActivityService) {
        super(desktopSessionService, instanceService, instanceSessionService, instanceActivityService);
    }

    @Override
    protected InstanceActivityType getControlActivityType(byte[] data) {

        ByteBuffer instructionWrapper = ByteBuffer.wrap(data, 16, 4).order(LITTLE_ENDIAN);
        int instructionType = instructionWrapper.getInt() & 0x000000ff;

        if (instructionType == 5) {
            return InstanceActivityType.MOUSE;

        } else if (instructionType == 6) {
            return InstanceActivityType.KEYBOARD;
        }

        return null;
    }

    @Override
    protected void writeData(ConnectionThread connectionThread, byte[] data) {
        connectionThread.writeByteData(data);
    }

}
