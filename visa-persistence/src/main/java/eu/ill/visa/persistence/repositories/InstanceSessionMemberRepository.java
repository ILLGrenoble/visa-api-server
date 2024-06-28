package eu.ill.visa.persistence.repositories;

import eu.ill.preql.FilterQuery;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.InstanceSession;
import eu.ill.visa.core.entity.InstanceSessionMember;
import eu.ill.visa.persistence.providers.InstanceSessionMemberFilterProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Singleton
public class InstanceSessionMemberRepository extends AbstractRepository<InstanceSessionMember> {

    @Inject
    InstanceSessionMemberRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<InstanceSessionMember> getAll() {
        final TypedQuery<InstanceSessionMember> query = getEntityManager()
            .createNamedQuery("instanceSessionMember.getAll", InstanceSessionMember.class);
        return query.getResultList();
    }

    public List<InstanceSessionMember> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final InstanceSessionMemberFilterProvider provider = new InstanceSessionMemberFilterProvider(getEntityManager());
        return super.getAll(provider, filter, orderBy, pagination);
    }

    public Long countAll(QueryFilter filter) {
        final InstanceSessionMemberFilterProvider provider = new InstanceSessionMemberFilterProvider(getEntityManager());
        final FilterQuery<InstanceSessionMember> query = createFilterQuery(provider, requireNonNullElseGet(filter, QueryFilter::new), null, null);
        query.addExpression((criteriaBuilder, root) ->
            criteriaBuilder.equal(root.get("active"), true)
        );
        return query.count();
    }

    public Long countAllActive(QueryFilter filter) {
        final InstanceSessionMemberFilterProvider provider = new InstanceSessionMemberFilterProvider(getEntityManager());
        final FilterQuery<InstanceSessionMember> query = createFilterQuery(provider, requireNonNullElseGet(filter, QueryFilter::new), null, null);
        query.addExpression((criteriaBuilder, root) -> criteriaBuilder.equal(root.get("active"), true));
        Date timeAgo = new Date(new Date().getTime() - 5 * 60 * 1000); // 5 minutes ago
        query.addExpression((criteriaBuilder, root) -> criteriaBuilder.greaterThan(root.get("lastInteractionAt"), timeAgo));
        return query.count();
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
