package eu.ill.visa.vdi.display.subscribers;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.business.services.DesktopSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class WebXRemoteDesktopEventSubscriber extends RemoteDesktopEventSubscriber<byte[]> {
    private static final Logger logger = LoggerFactory.getLogger(WebXRemoteDesktopEventSubscriber.class);

    private static final int INSTRUCTION_TYPE_OFFSET = 20;

    public WebXRemoteDesktopEventSubscriber(final DesktopSessionService desktopSessionService,
                                            final InstanceService instanceService) {
        super(desktopSessionService, instanceService);
    }

    @Override
    protected InstanceActivityType getControlActivityType(byte[] data) {

        ByteBuffer instructionWrapper = ByteBuffer.wrap(data, INSTRUCTION_TYPE_OFFSET, 4).order(LITTLE_ENDIAN);
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
