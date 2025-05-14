package eu.ill.visa.business.services;

import eu.ill.visa.business.MetricsConfiguration;
import eu.ill.visa.core.domain.Timer;
import eu.ill.visa.core.domain.metrics.LongGauge;
import eu.ill.visa.core.domain.metrics.LongHistogram;
import eu.ill.visa.core.domain.metrics.MetricProvider;
import eu.ill.visa.core.entity.Metric;
import eu.ill.visa.persistence.repositories.MetricRepository;
import io.quarkus.runtime.Shutdown;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Startup
@Transactional
@Singleton
public class MetricService {

    private static final Logger logger = LoggerFactory.getLogger(MetricService.class);

    private final MetricRepository repository;
    private final MetricsConfiguration metricsConfiguration;

    private final Cancellable exporterInterval;
    private final List<MetricProvider> metricProviders = new ArrayList<>();

    @Inject
    public MetricService(final MetricRepository repository,
                         final MetricsConfiguration metricsConfiguration) {
        this.repository = repository;
        this.metricsConfiguration = metricsConfiguration;

        if (this.metricsConfiguration.enabled()) {
            this.exporterInterval = Timer.setInterval(() -> {
                for (MetricProvider metricProvider : this.metricProviders) {
                    List<Metric> metrics = metricProvider.toMetrics();
                    for (Metric metric : metrics) {
                        this.save(metric);
                    }
                }

            }, this.metricsConfiguration.exportIntervalSeconds(), TimeUnit.SECONDS);

            logger.info("Metrics exporter enabled with interval of {} seconds", this.metricsConfiguration.exportIntervalSeconds());
            if ("visa".equals(metricsConfiguration.hostname())) {
                logger.warn("Metrics hostname is set to 'visa'. This should be changed to a unique hostname.");
            }

        } else {
            this.exporterInterval = null;
            logger.info("Metrics exporter disabled");
        }
    }

    @Shutdown
    public void shutdown() {
        if (this.exporterInterval != null) {
            this.exporterInterval.cancel();
        }
    }

    public LongHistogram createLongHistogram(String name) {
        LongHistogram histogram = new LongHistogram(name, this.metricsConfiguration.hostname());
        this.metricProviders.add(histogram);
        return histogram;
    }

    public LongGauge createLongGauge(String name) {
        LongGauge gauge = new LongGauge(name, this.metricsConfiguration.hostname());
        this.metricProviders.add(gauge);
        return gauge;
    }

    public void delete(Metric metric) {
        this.repository.delete(metric);
    }

    public void save(@NotNull Metric metric) {
        this.repository.save(metric);
    }

}
