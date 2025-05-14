package eu.ill.visa.core.domain.metrics;


import eu.ill.visa.core.entity.Metric;
import eu.ill.visa.core.entity.enumerations.MetricType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record LongHistogram(String name, String hostname, ArrayList<HistogramRecord<Long>> values) implements MetricProvider {

    public LongHistogram(String name, String hostname) {
        this(name, hostname, new ArrayList<>());
    }

    public void record(Long value) {
        synchronized (this.values) {
            this.values.add(new HistogramRecord<>(value));
        }
    }

    public void record(Long value, String attribute) {
        synchronized (this.values) {
            this.values.add(new HistogramRecord<>(value, attribute));
        }
    }

    public List<Metric> toMetrics() {
        ArrayList<HistogramRecord<Long>> copied;
        synchronized (this.values) {
            copied = new ArrayList<>(values);
            values.clear();
        }

        Map<String, List<HistogramRecord<Long>>> groupedByAttribute = copied.stream()
            .collect(Collectors.groupingBy(HistogramRecord::attribute));

        List<Metric> metrics = new ArrayList<>();

        groupedByAttribute.forEach((attribute, values) -> {
            long sum = values.stream().map(HistogramRecord::value).mapToLong(Long::longValue).sum();
            double average = values.stream().map(HistogramRecord::value).mapToLong(Long::longValue).average().orElse(0.0);
            long min = values.stream().map(HistogramRecord::value).mapToLong(Long::longValue).min().orElse(0L);
            long max = values.stream().map(HistogramRecord::value).mapToLong(Long::longValue).max().orElse(0L);

            Date minTimestamp = values.stream().map(HistogramRecord::timestamp).min(Date::compareTo).orElse(null);
            Date maxTimestamp = values.stream().map(HistogramRecord::timestamp).max(Date::compareTo).orElse(null);

            metrics.add(Metric.builder()
                .name(name)
                .host(hostname)
                .attribute(attribute)
                .statistic((double) sum)
                .type(MetricType.SUM)
                .recordCount((long) values.size())
                .periodStart(minTimestamp)
                .periodEnd(maxTimestamp)
                .build());

            metrics.add(Metric.builder()
                .name(name)
                .host(hostname)
                .attribute(attribute)
                .statistic(average)
                .type(MetricType.MEAN)
                .recordCount((long) values.size())
                .periodStart(minTimestamp)
                .periodEnd(maxTimestamp)
                .build());

            metrics.add(Metric.builder()
                .name(name)
                .host(hostname)
                .attribute(attribute)
                .statistic((double) min)
                .type(MetricType.MIN)
                .recordCount((long) values.size())
                .periodStart(minTimestamp)
                .periodEnd(maxTimestamp)
                .build());

            metrics.add(Metric.builder()
                .name(name)
                .host(hostname)
                .attribute(attribute)
                .statistic((double) max)
                .type(MetricType.MAX)
                .recordCount((long) values.size())
                .periodStart(minTimestamp)
                .periodEnd(maxTimestamp)
                .build());
        });

        return metrics;
    }

}
