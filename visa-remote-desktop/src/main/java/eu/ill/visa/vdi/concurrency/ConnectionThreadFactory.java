package eu.ill.visa.vdi.concurrency;

import javax.validation.constraints.NotNull;
import java.util.concurrent.ThreadFactory;

public class ConnectionThreadFactory implements ThreadFactory {

    private final static String NAME = "Virtual Desktop Thread";

    @Override
    public Thread newThread(@NotNull final Runnable runnable) {
        return new Thread(runnable, NAME);
    }
}
