package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.InstanceSessionMember;
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


    public InstanceSessionMember getSessionMember(final InstanceSession instanceSession, final String sessionId) {
        try {
            final TypedQuery<InstanceSessionMember> query = getEntityManager()
                .createNamedQuery("instanceSessionMember.getByInstanceSessionAndSessionId", InstanceSessionMember.class);
            query.setParameter("instanceSession", instanceSession);
            query.setParameter("sessionId", sessionId);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<InstanceSessionMember> getAllSessionMembersByInstanceId(final Long instanceId) {
        final TypedQuery<InstanceSessionMember> query = getEntityManager().createNamedQuery("instanceSessionMember.getAllForInstanceId", InstanceSessionMember.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    public List<InstanceSessionMember> getAllSessionMembersByInstanceIds(final List<Long> instanceIds) {
        final TypedQuery<InstanceSessionMember> query = getEntityManager().createNamedQuery("instanceSessionMember.getAllForInstanceIds", InstanceSessionMember.class);
        query.setParameter("instanceIds", instanceIds);
        return query.getResultList();
    }

    public List<InstanceSessionMember> getAllSessionMembersByInstanceSessionId(final Long instanceSessionId) {
        final TypedQuery<InstanceSessionMember> query = getEntityManager().createNamedQuery("instanceSessionMember.getAllByInstanceSessionId", InstanceSessionMember.class);
        query.setParameter("instanceSessionId", instanceSessionId);
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

    public InstanceSessionMember getBySessionId(String sessionId) {
        try {
            final TypedQuery<InstanceSessionMember> query = getEntityManager().createNamedQuery("instanceSessionMember.getBySessionId", InstanceSessionMember.class);
            query.setParameter("sessionId", sessionId);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }
}
