package eu.ill.visa.business.concurrent;

import eu.ill.visa.business.concurrent.actions.InstanceAction;
import eu.ill.visa.business.concurrent.actions.InstanceActionServiceProvider;
import eu.ill.visa.core.domain.InstanceCommand;
import eu.ill.visa.core.domain.enumerations.InstanceCommandState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyExceptionThrowingInstanceAction extends InstanceAction {

    private static final Logger log = LoggerFactory.getLogger(DummyExceptionThrowingInstanceAction.class);

    private Integer sleepDurationMs;

    public DummyExceptionThrowingInstanceAction(InstanceActionServiceProvider serviceProvider, InstanceCommand command, Integer sleepDurationMs) {
        super(serviceProvider, command);
        this.sleepDurationMs = sleepDurationMs;
    }

    @Override
    public void run() {
        log.info("Starting action");

        long sleepInterval = 100;
        long totalSleepTime = 0;

        while (totalSleepTime < this.sleepDurationMs / 2) {
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

        log.info("Action creating an exception");

        throw new RuntimeException("Dummy exception");
    }
}
