package eu.ill.visa.core.domain.metrics;


import java.util.ArrayList;
import java.util.List;

public record Meter(List<MetricProvider> metricProviders) {

    public Meter() {
        this(new ArrayList<MetricProvider>());
    }

    public LongHistogram createLongHistogram(String name) {
        LongHistogram histogram = new LongHistogram(name);
        this.metricProviders.add(histogram);
        return histogram;
    }

    public LongGauge createLongGauge(String name) {
        LongGauge gauge = new LongGauge(name);
        this.metricProviders.add(gauge);
        return gauge;
    }

}
