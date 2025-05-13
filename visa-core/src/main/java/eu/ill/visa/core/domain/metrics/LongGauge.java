package eu.ill.visa.core.domain.metrics;


import eu.ill.visa.core.entity.Metric;
import eu.ill.visa.core.entity.enumerations.MetricType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record LongGauge(String name, List<GaugeRecord<Long>> values) implements MetricProvider {

    LongGauge(String name) {
        this(name, Arrays.asList(new GaugeRecord<>(0L)));
    }

    LongGauge(String name, Long initialValue) {
        this(name, Arrays.asList(new GaugeRecord<>(initialValue)));
    }

    Long value() {
        synchronized (this.values) {
            return this.values.getLast().value();
        }
    }

    public void increaseBy(Long incBy) {
        synchronized (this.values) {
            this.values.add(new GaugeRecord<>(this.value() + incBy));
        }
    }

    public void decreaseBy(Long decValue) {
        synchronized (this.values) {
            this.values.add(new GaugeRecord<>(this.value() - decValue));
        }
    }

    public List<Metric> toMetrics() {
        ArrayList<GaugeRecord<Long>> copied;
        synchronized (this.values) {
            copied = new ArrayList<>(values);
            values.clear();
            values.add(new GaugeRecord<>(this.value()));
        }

        List<Metric> metrics = new ArrayList<>();

        if (!copied.isEmpty()) {
            long min = copied.stream().map(GaugeRecord::value).mapToLong(Long::longValue).min().orElse(0L);
            long max = copied.stream().map(GaugeRecord::value).mapToLong(Long::longValue).max().orElse(0L);
            long current = copied.getLast().value();

            metrics.add(Metric.builder()
                .name(name)
                .statistic((double) min)
                .type(MetricType.MIN)
                .recordCount((long)values.size())
                .periodStart(values.getFirst().timestamp())
                .periodEnd(values.getLast().timestamp())
                .build());

            metrics.add(Metric.builder()
                .name(name)
                .statistic((double) max)
                .type(MetricType.MAX)
                .recordCount((long)values.size())
                .periodStart(values.getFirst().timestamp())
                .periodEnd(values.getLast().timestamp())
                .build());

            metrics.add(Metric.builder()
                .name(name)
                .statistic((double) current)
                .type(MetricType.VALUE)
                .recordCount((long)values.size())
                .periodStart(values.getFirst().timestamp())
                .periodEnd(values.getLast().timestamp())
                .build());
        }

        return metrics;
    }

}
