package eu.ill.visa.core.domain.metrics;


import eu.ill.visa.core.entity.Metric;

import java.util.List;

public interface MetricProvider {
    List<Metric> toMetrics();
}
