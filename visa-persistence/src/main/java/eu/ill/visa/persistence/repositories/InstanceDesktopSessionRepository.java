package eu.ill.visa.persistence.repositories;

import eu.ill.visa.common.vdi.domain.ConnectedUser;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.entity.InstanceDesktopSession;
import eu.ill.visa.persistence.models.Tuple2;
import eu.ill.visa.persistence.utils.Tuple2Query;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@Singleton
public class InstanceDesktopSessionRepository extends AbstractRepository<InstanceDesktopSession> {
    private static final Logger logger = LoggerFactory.getLogger(InstanceDesktopSessionRepository.class);

    @Inject
    InstanceDesktopSessionRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public InstanceDesktopSession getById(final Long id) {
        try {
            final TypedQuery<InstanceDesktopSession> query = getEntityManager().createNamedQuery("instanceDesktopSession.getById", InstanceDesktopSession.class);
            query.setParameter("id", id);
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<InstanceDesktopSession> getAll() {
        return this.getAll(null);
    }

    public List<InstanceDesktopSession> getAll(Pagination pagination) {
        final TypedQuery<InstanceDesktopSession> query = getEntityManager().createNamedQuery("instanceDesktopSession.getAll", InstanceDesktopSession.class);
        if (pagination != null) {
            final int offset = pagination.getOffset();
            final int limit = pagination.getLimit();
            query.setFirstResult(offset);
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    public Long countAll() {
        final TypedQuery<Long> query = getEntityManager().createNamedQuery("instanceDesktopSession.countAll", Long.class);
        return query.getSingleResult();
    }

    public Long countAllActive() {
        final TypedQuery<Long> query = getEntityManager().createNamedQuery("instanceDesktopSession.countAllActive", Long.class);
        Date timeAgo = new Date(new Date().getTime() - 5 * 60 * 1000); // 5 minutes ago
        query.setParameter("timeAgo", timeAgo);
        return query.getSingleResult();
    }

    public List<InstanceDesktopSession> getAllByInstanceId(final Long instanceId) {
        final TypedQuery<InstanceDesktopSession> query = getEntityManager().createNamedQuery("instanceDesktopSession.getAllByInstanceId", InstanceDesktopSession.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    public InstanceDesktopSession getLatestByInstanceId(final Long instanceId) {
        try {
            final TypedQuery<InstanceDesktopSession> query = getEntityManager().createNamedQuery("instanceDesktopSession.getAllByInstanceId", InstanceDesktopSession.class);
            query.setParameter("instanceId", instanceId);
            query.setMaxResults(1); // Limit to the latest result
            query.setFirstResult(0);
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<InstanceDesktopSession> getAllByUserId(final String userId) {
        final TypedQuery<InstanceDesktopSession> query = getEntityManager().createNamedQuery("instanceDesktopSession.getAllByUserId", InstanceDesktopSession.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public List<InstanceDesktopSession> getAllByInstanceIds(final List<Long> instanceIds) {
        final TypedQuery<InstanceDesktopSession> query = getEntityManager().createNamedQuery("instanceDesktopSession.getAllByInstanceIds", InstanceDesktopSession.class);
        query.setParameter("instanceIds", instanceIds);
        return query.getResultList();
    }

    public List<InstanceDesktopSession> getAllHistoricalByInstanceId(final Long instanceId) {
        final TypedQuery<InstanceDesktopSession> query = getEntityManager().createNamedQuery("instanceDesktopSession.getAllHistoricalByInstanceId", InstanceDesktopSession.class);
        query.setParameter("instanceId", instanceId);
        return query.getResultList();
    }

    public List<InstanceDesktopSession> getAllByConnectionId(final String connectionId) {
        final TypedQuery<InstanceDesktopSession> query = getEntityManager().createNamedQuery("instanceDesktopSession.getAllByConnectionId", InstanceDesktopSession.class);
        query.setParameter("connectionId", connectionId);
        return query.getResultList();
    }

    public List<Tuple2<InstanceDesktopSession, ConnectedUser>> getAllWithConnectedUserByConnectionId(final String connectionId) throws Tuple2Query.Tuple2QueryException {
        final TypedQuery<Object[]> query = getEntityManager().createNamedQuery("instanceDesktopSession.getAllWithConnectedUserByConnectionId", Object[].class);
        query.setParameter("connectionId", connectionId);

        Tuple2Query<InstanceDesktopSession, ConnectedUser> tuple2Query = new Tuple2Query<>(query, InstanceDesktopSession.class, ConnectedUser.class);

        return tuple2Query.getResultList();
    }

    public List<ConnectedUser> getAllConnectedUsersByConnectionId(final String connectionId) {
        final TypedQuery<ConnectedUser> query = getEntityManager().createNamedQuery("instanceDesktopSession.getAllConnectedUsersByConnectionId", ConnectedUser.class);
        query.setParameter("connectionId", connectionId);

        return query.getResultList();
    }

    public List<ConnectedUser> getAllConnectedUsersByInstanceId(final Long instanceId) {
        final TypedQuery<ConnectedUser> query = getEntityManager().createNamedQuery("instanceDesktopSession.getAllConnectedUsersByInstanceId", ConnectedUser.class);
        query.setParameter("instanceId", instanceId);

        return query.getResultList();
    }

    public InstanceDesktopSession getByConnectionIdAndClientId(final String connectionId, final String clientId) {
        try {
            final TypedQuery<InstanceDesktopSession> query = getEntityManager().createNamedQuery("instanceDesktopSession.getByConnectionIdAndClientId", InstanceDesktopSession.class);
            query.setParameter("connectionId", connectionId);
            query.setParameter("clientId", clientId);
            query.setMaxResults(1); // Limit to the latest result
            query.setFirstResult(0);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void closeAll() {
        getEntityManager().createNamedQuery("instanceDesktopSession.closeAll")
            .executeUpdate();
    }

    public void cleanup() {
        getEntityManager().createNamedQuery("instanceDesktopSession.cleanup")
            .executeUpdate();
    }

    public int closeAllByInstanceIdAndConnectionIdAndClientId(final Long instanceId, final String connectionId, final String clientId) {
        return getEntityManager().createNamedQuery("instanceDesktopSession.closeAllByInstanceIdAndConnectionIdAndClientId")
            .setParameter("instanceId", instanceId)
            .setParameter("connectionId", connectionId)
            .setParameter("clientId", clientId)
            .executeUpdate();
    }

    public void close(final InstanceDesktopSession instanceDesktopSession) {
        getEntityManager().createNamedQuery("instanceDesktopSession.closeById")
            .setParameter("id", instanceDesktopSession.getId())
            .executeUpdate();
    }

    public void updateClientPingAt(final InstanceDesktopSession instanceDesktopSession) {
        getEntityManager().createNamedQuery("instanceDesktopSession.updateClientPingById")
            .setParameter("id", instanceDesktopSession.getId())
            .setParameter("lastClientPingAt", instanceDesktopSession.getLastClientPingAt())
            .executeUpdate();
    }

    public void updateInteractionAt(final InstanceDesktopSession instanceDesktopSession) {
        getEntityManager().createNamedQuery("instanceDesktopSession.updateInteractionAtById")
            .setParameter("id", instanceDesktopSession.getId())
            .setParameter("lastInteractionAt", instanceDesktopSession.getLastInteractionAt())
            .executeUpdate();
    }


    public void save(final InstanceDesktopSession instanceDesktopSession) {
        if (instanceDesktopSession.getId() == null) {
            persist(instanceDesktopSession);
        } else {
            merge(instanceDesktopSession);
        }
    }

}
