package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.DesktopSessionRttSample;
import eu.ill.visa.core.entity.Hypervisor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Singleton
public class DesktopSessionRttSampleRepository extends AbstractRepository<DesktopSessionRttSample> {

    public record HypervisorSample(Hypervisor hypervisor, DesktopSessionRttSample sample) {}

    @Inject
    DesktopSessionRttSampleRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<DesktopSessionRttSample> getBySessionId(Long sessionId) {
        final TypedQuery<DesktopSessionRttSample> query = getEntityManager().createNamedQuery("desktopSessionRttSample.getBySessionId", DesktopSessionRttSample.class);
        query.setParameter("sessionId", sessionId);
        return query.getResultList();
    }

    public List<DesktopSessionRttSample> getSessionIds(List<Long> sessionIds) {
        final TypedQuery<DesktopSessionRttSample> query = getEntityManager().createNamedQuery("desktopSessionRttSample.getBySessionIds", DesktopSessionRttSample.class);
        query.setParameter("sessionIds", sessionIds);
        return query.getResultList();
    }

    public List<DesktopSessionRttSample> getByInstanceId(Long instanceId) {
        final TypedQuery<DesktopSessionRttSample> query = getEntityManager().createNamedQuery("desktopSessionRttSample.getByInstanceId", DesktopSessionRttSample.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    public List<DesktopSessionRttSample> getAllRequiringResampling(int daysOld) {
        final TypedQuery<DesktopSessionRttSample> query = getEntityManager().createNamedQuery("desktopSessionRttSample.getAllRequiringResampling", DesktopSessionRttSample.class);
        Date cutoff = Date.from(Instant.now().minus(daysOld, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS));
        query.setParameter("date", cutoff);
        return query.getResultList();
    }

    public List<HypervisorSample> getByRecentHypervisorSamples(int minutesOld) {
        final TypedQuery<Object[]> query = getEntityManager().createNamedQuery("desktopSessionRttSample.getByHypervisorSamplesSinceDate", Object[].class);
        Date cutoff = Date.from(Instant.now().minus(minutesOld, ChronoUnit.MINUTES));

        query.setParameter("date", cutoff);
        return query.getResultList().stream().map(array -> {
            Hypervisor hypervisor = (Hypervisor) array[0];
            DesktopSessionRttSample sample = (DesktopSessionRttSample) array[1];
            return new HypervisorSample(hypervisor, sample);
        }).toList();
    }

    public void deleteAll(Collection<DesktopSessionRttSample> samples) {
        if (samples.isEmpty()) {
            return;
        }

        List<Long> ids = samples.stream().map(DesktopSessionRttSample::getId).toList();

        getEntityManager().createNamedQuery("desktopSessionRttSample.deleteByIds")
            .setParameter("ids", ids)
            .executeUpdate();
    }

    public void save(final DesktopSessionRttSample sample) {
        if (sample.getId() == null) {
            persist(sample);
        } else {
            merge(sample);
        }
    }
}
