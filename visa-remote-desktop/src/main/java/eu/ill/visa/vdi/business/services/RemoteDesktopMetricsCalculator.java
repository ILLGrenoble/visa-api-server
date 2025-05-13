package eu.ill.visa.vdi.business.services;

import eu.ill.visa.core.domain.Timer;
import io.quarkus.runtime.Shutdown;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class RemoteDesktopMetricsCalculator {
    private final static Logger logger = LoggerFactory.getLogger(RemoteDesktopMetricsCalculator.class);

    private record RemoteDesktopEventMetric(String type, Long durationMicroseconds) {}

    private final List<RemoteDesktopEventMetric> eventDurationMetrics = new LinkedList<>();
    private final List<RemoteDesktopEventMetric> eventStartDelayMetrics = new LinkedList<>();
    private final AtomicInteger guacamoleSessionMembers = new AtomicInteger(0);
    private final AtomicInteger webXSessionMembers = new AtomicInteger(0);

    private final Cancellable calculatorInterval;

    public RemoteDesktopMetricsCalculator() {
        this.calculatorInterval = Timer.setInterval(() -> {
            long eventDurationSum = this.eventDurationMetrics.stream().mapToLong(RemoteDesktopEventMetric::durationMicroseconds).sum();
            long eventDurationCount = this.eventDurationMetrics.size();
            long eventDurationMax = this.eventDurationMetrics.stream().mapToLong(RemoteDesktopEventMetric::durationMicroseconds).max().orElse(0);
            long eventDurationAverage = eventDurationCount > 0 ? eventDurationSum / eventDurationCount : 0;

            this.eventDurationMetrics.clear();

            long eventStartDelaySum = this.eventStartDelayMetrics.stream().mapToLong(RemoteDesktopEventMetric::durationMicroseconds).sum();
            long eventStartDelayCount = this.eventStartDelayMetrics.size();
            long eventStartDelayMax = this.eventStartDelayMetrics.stream().mapToLong(RemoteDesktopEventMetric::durationMicroseconds).max().orElse(0);
            long eventStartDelayAverage = eventStartDelayCount > 0 ? eventStartDelaySum / eventStartDelayCount : 0;

            this.eventStartDelayMetrics.clear();

            if (eventDurationCount > 0) {
                logger.info("Remote Desktop Metrics: Event Duration - Average: {}ms, Max: {}ms, Count: {}",
                        eventDurationAverage / 1000.0, eventDurationMax / 1000.0, eventDurationCount);
            }
            if (eventStartDelayCount > 0) {
                logger.info("Remote Desktop Metrics: Event Start Delay - Average: {}ms, Max: {}ms, Count: {}",
                        eventStartDelayAverage / 1000.0, eventStartDelayMax / 1000.0, eventStartDelayCount);
            }

            if (this.guacamoleSessionMembers.get() > 0 || this.webXSessionMembers.get() > 0) {
                logger.info("Number of remote desktop session members: Guacamole: {}, WebX: {}", this.guacamoleSessionMembers.get(), this.webXSessionMembers.get());
            }

        }, 10, TimeUnit.SECONDS);
    }

    @Shutdown
    public void shutdown() {
        this.calculatorInterval.cancel();
    }


    public void addEventDurationMetric(final String type, final Long durationMicroseconds) {
        this.eventDurationMetrics.add(new RemoteDesktopEventMetric(type, durationMicroseconds));
    }

    public void addEventStartDelayMetric(final String type, final Long delayMicroseconds) {
        this.eventStartDelayMetrics.add(new RemoteDesktopEventMetric(type, delayMicroseconds));
    }

    public AtomicInteger getGuacamoleSessionMembers() {
        return guacamoleSessionMembers;
    }

    public AtomicInteger getWebXSessionMembers() {
        return webXSessionMembers;
    }
}
