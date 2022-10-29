package eu.ill.visa.vdi.listeners;

import eu.ill.visa.business.services.InstanceActivityService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.domain.enumerations.InstanceActivityType;
import eu.ill.visa.vdi.concurrency.ConnectionThread;
import eu.ill.visa.vdi.services.DesktopConnectionService;

import java.nio.ByteBuffer;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class WebXClientDisplayListener extends ClientDisplayListener<byte[]> {

    public WebXClientDisplayListener(final DesktopConnectionService desktopConnectionService,
                                     final InstanceService instanceService,
                                     final InstanceSessionService instanceSessionService,
                                     final InstanceActivityService instanceActivityService) {
        super(desktopConnectionService, instanceService, instanceSessionService, instanceActivityService);
    }

    @Override
    protected InstanceActivityType getControlActivityType(byte[] data) {

        ByteBuffer instructionWrapper = ByteBuffer.wrap(data, 16, 1).order(LITTLE_ENDIAN);
        int instructionType = instructionWrapper.getInt();

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
