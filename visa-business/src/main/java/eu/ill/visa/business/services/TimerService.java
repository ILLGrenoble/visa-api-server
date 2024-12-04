package eu.ill.visa.business.services;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class TimerService {

    public static Cancellable setTimeout(Runnable task, long delay, TimeUnit unit) {
        return Uni.createFrom().voidItem()
            .onItem()
            .delayIt().by(Duration.ofMillis(unit.toMillis(delay)))
            .subscribe()
            .with(ignored -> task.run());
    }

    public static Cancellable setInterval(Runnable task, long interval, TimeUnit unit) {
        return Multi.createFrom().ticks()
            .every(Duration.ofMillis(unit.toMillis(interval)))
            .subscribe()
            .with(ignored -> task.run());
    }
}
