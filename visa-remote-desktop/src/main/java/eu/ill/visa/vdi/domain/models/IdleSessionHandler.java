package eu.ill.visa.vdi.domain.models;


import eu.ill.visa.business.services.TimerService;
import io.smallrye.mutiny.subscription.Cancellable;

import java.util.concurrent.TimeUnit;

public class IdleSessionHandler {

    private static final int IDLE_TIMEOUT_SECONDS = 30;

    private final boolean enabled;
    private Runnable onIdleCallback;
    private Cancellable timer;

    public IdleSessionHandler(boolean enabled) {
        this.enabled = enabled;
    }

    public void start(Runnable onIdleCallback) {
        if (enabled) {
            if (this.timer != null) {
                this.timer.cancel();
            }
            this.onIdleCallback = onIdleCallback;
            this.createTimer();
        }
    }

    public void reset() {
        if (this.timer != null) {
            this.timer.cancel();
            this.createTimer();
        }
    }

    public void stop() {
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    private void createTimer() {
        this.timer = TimerService.setTimeout(() -> {
            this.onIdleCallback.run();
        }, IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
