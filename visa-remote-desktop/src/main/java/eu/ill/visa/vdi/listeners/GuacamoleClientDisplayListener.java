package eu.ill.visa.vdi.listeners;

import eu.ill.visa.business.services.InstanceActivityService;
import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.core.domain.enumerations.InstanceActivityType;
import eu.ill.visa.vdi.concurrency.ConnectionThread;
import eu.ill.visa.vdi.services.DesktopConnectionService;

public class GuacamoleClientDisplayListener extends ClientDisplayListener<String> {

    public GuacamoleClientDisplayListener(final DesktopConnectionService desktopConnectionService,
                                          final InstanceService instanceService,
                                          final InstanceSessionService instanceSessionService,
                                          final InstanceActivityService instanceActivityService) {
        super(desktopConnectionService, instanceService, instanceSessionService, instanceActivityService);
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
