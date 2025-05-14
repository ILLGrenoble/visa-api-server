package eu.ill.visa.core.domain.metrics;


import eu.ill.visa.core.entity.Metric;
import eu.ill.visa.core.entity.enumerations.MetricType;

import java.util.ArrayList;
import java.util.List;

public record LongGauge(String name, String hostname, List<GaugeRecord<Long>> values) implements MetricProvider {

    public LongGauge(String name, String hostname) {
        this(name, hostname, new ArrayList<>(List.of(new GaugeRecord<>(0L))));
    }

    public LongGauge(String name, String hostname, Long initialValue) {
        this(name, hostname, new ArrayList<>(List.of(new GaugeRecord<>(initialValue))));
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

    public void set(Long value) {
        synchronized (this.values) {
            this.values.add(new GaugeRecord<>(value));
        }
    }

    public List<Metric> toMetrics() {
        ArrayList<GaugeRecord<Long>> copied;
        synchronized (this.values) {
            copied = new ArrayList<>(values);
            Long current = copied.getLast().value();
            values.clear();
            values.add(new GaugeRecord<>(current));
        }

        List<Metric> metrics = new ArrayList<>();

        if (!copied.isEmpty()) {
            long min = copied.stream().map(GaugeRecord::value).mapToLong(Long::longValue).min().orElse(0L);
            long max = copied.stream().map(GaugeRecord::value).mapToLong(Long::longValue).max().orElse(0L);
            long current = copied.getLast().value();

            metrics.add(Metric.builder()
                .name(name)
                .host(hostname)
                .statistic((double) min)
                .type(MetricType.MIN)
                .recordCount((long)values.size())
                .periodStart(values.getFirst().timestamp())
                .periodEnd(values.getLast().timestamp())
                .build());

            metrics.add(Metric.builder()
                .name(name)
                .host(hostname)
                .statistic((double) max)
                .type(MetricType.MAX)
                .recordCount((long)values.size())
                .periodStart(values.getFirst().timestamp())
                .periodEnd(values.getLast().timestamp())
                .build());

            metrics.add(Metric.builder()
                .name(name)
                .host(hostname)
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
