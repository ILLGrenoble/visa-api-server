package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.DesktopSessionRttSample;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class DesktopSessionRttSampleRepository extends AbstractRepository<DesktopSessionRttSample> {

    @Inject
    DesktopSessionRttSampleRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<DesktopSessionRttSample> getByInstanceSessionMemberId(Long instanceSessionMemberId) {
        final TypedQuery<DesktopSessionRttSample> query = getEntityManager().createNamedQuery("desktopSessionRttSample.getByInstanceSessionMemberId", DesktopSessionRttSample.class);
        query.setParameter("instanceSessionMemberId", instanceSessionMemberId);
        return query.getResultList();
    }

    public List<DesktopSessionRttSample> getByInstanceSessionMemberIds(List<Long> instanceSessionMemberIds) {
        final TypedQuery<DesktopSessionRttSample> query = getEntityManager().createNamedQuery("desktopSessionRttSample.getByInstanceSessionMemberIds", DesktopSessionRttSample.class);
        query.setParameter("instanceSessionMemberIds", instanceSessionMemberIds);
        return query.getResultList();
    }

    public List<DesktopSessionRttSample> getByInstanceId(Long instanceId) {
        final TypedQuery<DesktopSessionRttSample> query = getEntityManager().createNamedQuery("desktopSessionRttSample.getByInstanceId", DesktopSessionRttSample.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    public void save(final DesktopSessionRttSample sample) {
        if (sample.getId() == null) {
            persist(sample);
        } else {
            merge(sample);
        }
    }
}
