package eu.ill.visa.core.domain;


import io.smallrye.mutiny.subscription.Cancellable;

import java.util.concurrent.TimeUnit;

public class IdleHandler {

    private final int timeoutSeconds;
    private final boolean enabled;
    private Runnable onIdleCallback;
    private Cancellable timer;

    public IdleHandler(int timeoutSeconds) {
        this.enabled = true;
        this.timeoutSeconds = timeoutSeconds;
    }

    public IdleHandler(boolean enabled, int timeoutSeconds) {
        this.enabled = enabled;
        this.timeoutSeconds = timeoutSeconds;
    }

    public synchronized void start(Runnable onIdleCallback) {
        if (enabled) {
            if (this.timer != null) {
                this.timer.cancel();
            }
            this.onIdleCallback = onIdleCallback;
            this.createTimer();
        }
    }

    public synchronized void reset() {
        if (this.timer != null) {
            this.timer.cancel();
            this.createTimer();
        }
    }

    public synchronized void stop() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    private void createTimer() {
        this.timer = Timer.setTimeout(() -> {
            this.onIdleCallback.run();
        }, timeoutSeconds, TimeUnit.SECONDS);
    }
}
