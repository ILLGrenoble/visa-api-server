package eu.ill.visa.business.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;

public class InstanceActionThreadFactory implements ThreadFactory {

    private static class TaskThread extends Thread {

        private static final Logger log = LoggerFactory.getLogger(InstanceActionThreadFactory.class);

        private static final ThreadLocal<String> threadName = new ThreadLocal<String>();

        public TaskThread(Runnable target) {
            super(target);
        }

        @Override
        public void run() {
            // Initialise any necessary things for the thread here (keep them available for all later usage)
            getThreadName();

            super.run();
        }

        public static String getThreadName() {
            if (threadName.get() == null) {
                threadName.set("InstanceActionThread-" + Thread.currentThread().getId());
                log.debug("Creating new thread " + threadName.get());
            }

            return threadName.get();
        }
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new TaskThread(runnable);
    }
}
