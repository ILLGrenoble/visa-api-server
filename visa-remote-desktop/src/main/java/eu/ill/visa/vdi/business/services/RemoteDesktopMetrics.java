package eu.ill.visa.vdi.business.services;


import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class RemoteDesktopMetrics {

    private final LongHistogram eventDurationHistogram;
    private final LongHistogram eventStartDelayHistogram;
    private final AtomicInteger totalDesktopSessionMembers = new AtomicInteger(0);
    private final AtomicInteger guacamoleSessionMembers = new AtomicInteger(0);
    private final AtomicInteger webXSessionMembers = new AtomicInteger(0);

    @Inject
    public RemoteDesktopMetrics(Meter meter) {
        this.eventDurationHistogram = meter
            .histogramBuilder("visa.vdi.event.duration.ms")
            .setDescription("Duration of Remote Desktop events")
            .setUnit("ms")
            .ofLongs()
            .build();

        this.eventStartDelayHistogram = meter
            .histogramBuilder("visa.vdi.event.start.delay.ms")
            .setDescription("Delay before executing Remote Desktop events")
            .setUnit("ms")
            .ofLongs()
            .build();
        meter.gaugeBuilder("visa.vdi.desktop.session.members.total")
            .setDescription("Total number of connected desktop session members")
            .setUnit("members")
            .ofLongs()
            .buildWithCallback(result ->
                result.record(totalDesktopSessionMembers.get())
            );

        meter.gaugeBuilder("visa.vdi.desktop.session.members.guacamole")
            .setDescription("Number of connected Guacamole desktop session members")
            .setUnit("members")
            .ofLongs()
            .buildWithCallback(result ->
                result.record(guacamoleSessionMembers.get())
            );

        meter.gaugeBuilder("visa.vdi.desktop.session.members.webx")
            .setDescription("Number of connected WebX desktop session members")
            .setUnit("members")
            .ofLongs()
            .buildWithCallback(result ->
                result.record(webXSessionMembers.get())
            );
    }

    public LongHistogram getEventDurationHistogram() {
        return eventDurationHistogram;
    }

    public LongHistogram getEventStartDelayHistogram() {
        return eventStartDelayHistogram;
    }

    public AtomicInteger getTotalDesktopSessionMembers() {
        return totalDesktopSessionMembers;
    }

    public AtomicInteger getGuacamoleSessionMembers() {
        return guacamoleSessionMembers;
    }

    public AtomicInteger getWebXSessionMembers() {
        return webXSessionMembers;
    }
}
