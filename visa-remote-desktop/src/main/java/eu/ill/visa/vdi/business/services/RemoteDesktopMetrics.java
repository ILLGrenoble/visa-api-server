package eu.ill.visa.vdi.business.services;

import eu.ill.visa.business.services.MetricService;
import eu.ill.visa.core.domain.metrics.LongGauge;
import eu.ill.visa.core.domain.metrics.LongHistogram;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@ApplicationScoped
public class RemoteDesktopMetrics {
    private final static Logger logger = LoggerFactory.getLogger(RemoteDesktopMetrics.class);

    private final LongHistogram eventDurationHistogram;
    private final LongHistogram eventStartDelayHistogram;
    private final LongGauge totalDesktopSessionMembers;
    private final LongGauge guacamoleDesktopSessionMembers;
    private final LongGauge webxDesktopSessionMembers;

    @Inject
    public RemoteDesktopMetrics(final MetricService metricService) {
        this.eventDurationHistogram = metricService.createLongHistogram("visa.vdi.event.duration.micros");
        this.eventStartDelayHistogram = metricService.createLongHistogram("visa.vdi.event.start.delay.micros");
        this.totalDesktopSessionMembers = metricService.createLongGauge("visa.vdi.total.session.members");
        this.guacamoleDesktopSessionMembers = metricService.createLongGauge("visa.vdi.guacamole.session.members");
        this.webxDesktopSessionMembers = metricService.createLongGauge("visa.vdi.webx.session.members");
    }

    public LongHistogram getEventDurationHistogram() {
        return eventDurationHistogram;
    }

    public LongHistogram getEventStartDelayHistogram() {
        return eventStartDelayHistogram;
    }

    public LongGauge getTotalDesktopSessionMembers() {
        return totalDesktopSessionMembers;
    }

    public LongGauge getGuacamoleDesktopSessionMembers() {
        return guacamoleDesktopSessionMembers;
    }

    public LongGauge getWebXDesktopSessionMembers() {
        return webxDesktopSessionMembers;
    }

}
