package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.InstanceSessionMember;
import eu.ill.visa.core.entity.partial.InstanceSessionMemberPartial;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;

@Singleton
public class InstanceSessionMemberRepository extends AbstractRepository<InstanceSessionMember> {

    @Inject
    InstanceSessionMemberRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<InstanceSessionMember> getAll() {
        return this.getAll(null);
    }

    public List<InstanceSessionMember> getAll(Pagination pagination) {
        final TypedQuery<InstanceSessionMember> query = getEntityManager().createNamedQuery("instanceSessionMember.getAll", InstanceSessionMember.class);
        if (pagination != null) {
            final int offset = pagination.getOffset();
            final int limit = pagination.getLimit();
            query.setFirstResult(offset);
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    public Long countAll() {
        final TypedQuery<Long> query = getEntityManager().createNamedQuery("instanceSessionMember.countAll", Long.class);
        return query.getSingleResult();
    }

    public Long countAllActive() {
        final TypedQuery<Long> query = getEntityManager().createNamedQuery("instanceSessionMember.countAllActive", Long.class);
        Date timeAgo = new Date(new Date().getTime() - 5 * 60 * 1000); // 5 minutes ago
        query.setParameter("timeAgo", timeAgo);
        return query.getSingleResult();
    }


    public List<InstanceSessionMember> getAllByInstanceId(final Long instanceId) {
        final TypedQuery<InstanceSessionMember> query = getEntityManager().createNamedQuery("instanceSessionMember.getAllForInstanceId", InstanceSessionMember.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    public List<InstanceSessionMember> getAllByInstanceIds(final List<Long> instanceIds) {
        final TypedQuery<InstanceSessionMember> query = getEntityManager().createNamedQuery("instanceSessionMember.getAllForInstanceIds", InstanceSessionMember.class);
        query.setParameter("instanceIds", instanceIds);
        return query.getResultList();
    }

    public List<InstanceSessionMember> getAllHistorySessionMembersByInstanceId(final Long instanceId) {
        final TypedQuery<InstanceSessionMember> query = getEntityManager().createNamedQuery("instanceSessionMember.getAllHistoryForInstanceId", InstanceSessionMember.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }


    public void save(final InstanceSessionMember instanceSessionMember) {
        if (instanceSessionMember.getId() == null) {
            persist(instanceSessionMember);
        } else {
            merge(instanceSessionMember);
        }
    }

    public InstanceSessionMemberPartial getPartialByInstanceSessionIdAndClientId(final Long instanceSessionId, final String clientId) {
        try {
            final TypedQuery<InstanceSessionMemberPartial> query = getEntityManager().createNamedQuery("instanceSessionMember.getPartialByInstanceSessionIdAndClientId", InstanceSessionMemberPartial.class);
            query.setParameter("instanceSessionId", instanceSessionId);
            query.setParameter("clientId", clientId);
            query.setMaxResults(1); // Limit to the latest result
            query.setFirstResult(0);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<InstanceSessionMemberPartial> getAllPartials() {
        final TypedQuery<InstanceSessionMemberPartial> query = getEntityManager().createNamedQuery("instanceSessionMember.getAllPartials", InstanceSessionMemberPartial.class);
        return query.getResultList();
    }

    public List<InstanceSessionMemberPartial> getAllPartialsByInstanceId(final Long instanceId) {
        final TypedQuery<InstanceSessionMemberPartial> query = getEntityManager().createNamedQuery("instanceSessionMember.getAllPartialsForInstanceId", InstanceSessionMemberPartial.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    public List<InstanceSessionMemberPartial> getAllPartialsByInstanceSessionId(final Long instanceSessionId) {
        final TypedQuery<InstanceSessionMemberPartial> query = getEntityManager().createNamedQuery("instanceSessionMember.getAllPartialByInstanceSessionId", InstanceSessionMemberPartial.class);
        query.setParameter("instanceSessionId", instanceSessionId);
        return query.getResultList();
    }

    public void deactivateSessionMember(final InstanceSessionMemberPartial instanceSessionMember) {
        getEntityManager().createNamedQuery("instanceSessionMember.deactivateById")
            .setParameter("id", instanceSessionMember.getId())
            .executeUpdate();
    }

    public void updateInteractionAt(final InstanceSessionMemberPartial instanceSessionMember) {
        getEntityManager().createNamedQuery("instanceSessionMember.updateInteractionAtById")
            .setParameter("id", instanceSessionMember.getId())
            .setParameter("lastInteractionAt", instanceSessionMember.getLastInteractionAt())
            .executeUpdate();
    }

    public int deactivateAllByInstanceSessionIdAndClientID(final Long instanceSessionId, final String clientId) {
        return getEntityManager().createNamedQuery("instanceSessionMember.deactivateAllByInstanceSessionIdAndClientId")
            .setParameter("instanceSessionId", instanceSessionId)
            .setParameter("clientId", clientId)
            .executeUpdate();
    }
}
