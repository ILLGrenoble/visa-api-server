package eu.ill.visa.vdi.display.subscribers;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.enumerations.InstanceActivityType;
import eu.ill.visa.vdi.business.concurrency.ConnectionThread;
import eu.ill.visa.vdi.business.services.DesktopSessionService;

public class GuacamoleRemoteDesktopEventSubscriber extends RemoteDesktopEventSubscriber<String> {

    public GuacamoleRemoteDesktopEventSubscriber(final DesktopSessionService desktopSessionService,
                                                 final InstanceService instanceService) {
        super(desktopSessionService, instanceService);
    }

    @Override
    protected InstanceActivityType getControlActivityType(String data) {
        int separatorPos = data.indexOf('.');
        int commandLength = Integer.parseInt(data.substring(0, separatorPos));
        String command = data.substring(separatorPos + 1, separatorPos + 1 + commandLength);

        if (command.equals("mouse")) {
            return InstanceActivityType.MOUSE;

        } else if (command.equals("key")){
            return InstanceActivityType.KEYBOARD;
        }

        return null;
    }

    @Override
    protected void writeData(ConnectionThread connectionThread, String data) {
        connectionThread.writeCharData(data.toCharArray());
    }
}
