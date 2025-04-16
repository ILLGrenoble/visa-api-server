package eu.ill.visa.core.domain;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Timer {

    public static class GuardedTimeout implements Cancellable {
        private final AtomicBoolean cancelled;
        private final Cancellable cancellable;

        public GuardedTimeout(AtomicBoolean cancelled, Cancellable cancellable) {
            this.cancelled = cancelled;
            this.cancellable = cancellable;
        }

        @Override
        public void cancel() {
            cancelled.set(true);
            cancellable.cancel();
        }
    }

    public static Cancellable setTimeout(Runnable task, long delay, TimeUnit unit) {
        AtomicBoolean cancelled = new AtomicBoolean(false);

        Cancellable cancellable = Uni.createFrom().voidItem()
            .onItem()
            .delayIt().by(Duration.ofMillis(unit.toMillis(delay)))
            .subscribe()
            .with(ignored -> {
                if (!cancelled.get()) {
                    task.run();
                }
            });

        return new GuardedTimeout(cancelled, cancellable);
    }

    public static Cancellable setInterval(Runnable task, long interval, TimeUnit unit) {
        return Multi.createFrom().ticks()
            .every(Duration.ofMillis(unit.toMillis(interval)))
            .subscribe()
            .with(ignored -> task.run());
    }

    public static class ReusableTimer {
        private Cancellable timer;
        private final Runnable task;
        private final long delay;
        private final TimeUnit unit;

        public ReusableTimer(final Runnable task, long delay, TimeUnit unit) {
            this.task = task;
            this.delay = delay;
            this.unit = unit;
        }

        public synchronized void start() {
            if (this.timer == null) {
                this.timer = Timer.setTimeout(this::run, this.delay, this.unit);
            }
        }

        public synchronized void stop() {
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
        }

        private void run() {
            this.timer = null;
            this.task.run();
        }
    }
}
