package eu.ill.visa.vdi.business.concurrency;

import java.util.concurrent.ThreadFactory;

public class ConnectionThreadFactory implements ThreadFactory {

    private final static String NAME = "Virtual Desktop Thread";

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, NAME);
    }
}
