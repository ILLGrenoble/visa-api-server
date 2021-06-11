package eu.ill.visa.business.concurrent;

import eu.ill.visa.business.concurrent.actions.InstanceAction;
import eu.ill.visa.business.concurrent.actions.InstanceActionServiceProvider;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.enumerations.InstanceCommandState;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyInstanceAction extends InstanceAction {

    private static final Logger log = LoggerFactory.getLogger(DummyInstanceAction.class);

    private Integer sleepDurationMs;

    public DummyInstanceAction(InstanceActionServiceProvider serviceProvider, InstanceCommand command, Integer sleepDurationMs) {
        super(serviceProvider, command);
        this.sleepDurationMs = sleepDurationMs;
    }

    @Override
    public void run() {
        log.info("Starting action");

        long sleepInterval = 100;
        long totalSleepTime = 0;

        while (totalSleepTime < this.sleepDurationMs) {
            try {
                Thread.sleep(sleepInterval);

                if (this.getCommandStateFromDatabase().equals(InstanceCommandState.CANCELLED)) {
                    log.info("Command cancelled");
                    return;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            totalSleepTime += sleepInterval;
        }

        this.updateInstanceState(InstanceState.ACTIVE);

        log.info("Finished action");
    }
}
