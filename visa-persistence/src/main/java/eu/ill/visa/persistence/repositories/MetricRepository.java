package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Metric;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;

@Singleton
public class MetricRepository extends AbstractRepository<Metric> {

    @Inject
    MetricRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public void delete(final Metric metric) {
        remove(metric);
    }

    public void save(final Metric metric) {
        if (metric.getId() == null) {
            persist(metric);

        } else {
            merge(metric);
        }
    }
}
