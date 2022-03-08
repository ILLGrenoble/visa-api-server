package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.preql.FilterQuery;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.domain.enumerations.InstanceMemberRole;
import eu.ill.visa.core.domain.enumerations.InstanceState;
import eu.ill.visa.persistence.providers.InstanceFilterProvider;
import org.apache.commons.lang3.time.DateUtils;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import static java.util.Objects.requireNonNullElseGet;

@Singleton
public class InstanceRepository extends AbstractRepository<Instance> {

    @Inject
    InstanceRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<Instance> getAll() {
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAll", Instance.class);
        return query.getResultList();
    }

    public List<Instance> getAllForUser(User user) {
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllForUser", Instance.class);
        query.setParameter("user", user);

        return query.getResultList();
    }

    public List<Instance> getAllWithStates(List<InstanceState> states) {
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllWithStates", Instance.class);
        query.setParameter("states", states);

        return query.getResultList();
    }

    public Instance getInstanceForMember(InstanceMember member) {
        try {
            final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getInstanceForMember", Instance.class);
            query.setParameter("member", member);

            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    public Instance getById(final Long id) {
        try {
            final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getById", Instance.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public Instance getByUID(final String uid) {
        try {
            final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getByUID", Instance.class);
            query.setParameter("uid", uid);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void save(Instance instance) {
        if (instance.getId() == null) {
            persist(instance);

        } else {
            merge(instance);
        }
    }

    public Long countAll() {
        try {
            final TypedQuery<Long> query = getEntityManager().createNamedQuery("instance.countAll", Long.class);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public Long countAllForState(InstanceState state) {
        try {
            final TypedQuery<Long> query = getEntityManager().createNamedQuery("instance.countAllForState", Long.class);
            query.setParameter("state", state);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<Instance> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final InstanceFilterProvider provider = new InstanceFilterProvider(getEntityManager());
        final FilterQuery<Instance> query = createFilterQuery(provider, requireNonNullElseGet(filter, QueryFilter::new), orderBy, pagination);
        query.addExpression((criteriaBuilder, root) ->
            criteriaBuilder.isNull(root.get("deletedAt"))
        );
        return query.getResultList();
    }

    public Long countAll(QueryFilter filter) {
        final InstanceFilterProvider provider = new InstanceFilterProvider(getEntityManager());
        final FilterQuery<Instance> query = createFilterQuery(provider, requireNonNullElseGet(filter, QueryFilter::new), null, null);
        query.addExpression((criteriaBuilder, root) ->
            criteriaBuilder.isNull(root.get("deletedAt"))
        );
        return query.count();
    }


    /**
     * Get all instances that will soon expire due to inactivity
     *
     * @param inactiveInHours the duration in hours an instance has been inactive
     */
    public List<Instance> getAllInactive(int inactiveInHours) {
        final Date date = DateUtils.addHours(new Date(), -inactiveInHours);
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllInactive", Instance.class);
        query.setParameter("date", date);

        return query.getResultList();
    }

    /**
     * Get all instances that will soon expire due to inactivity that are not already scheduled to be deleted
     *
     * @param inactiveInHours the duration in hours an instance has been inactive
     */
    public List<Instance> getAllNewInactive(int inactiveInHours) {
        final Date date = DateUtils.addHours(new Date(), -inactiveInHours);
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllNewInactive", Instance.class);
        query.setParameter("date", date);

        return query.getResultList();
    }

    /**
     * Get all instances that will soon expire due to their termination date that are not already scheduled to be deleted
     *
     * @param terminationInHours the number of hours to when the instance will expire due to their termination date
     */
    public List<Instance> getAllNewTerminations(int terminationInHours) {
        final Date date = DateUtils.addHours(new Date(), terminationInHours);
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllNewTerminations", Instance.class);
        query.setParameter("date", date);

        return query.getResultList();
    }

    public List<Instance> getAllToDelete() {
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllToDelete", Instance.class);
        return query.getResultList();
    }

    /**
     * Count all instances that are visible for an instrument scientist (instrument match)
     */
    public Long countAllForInstrumentScientist(User user, InstanceFilter filter) {
        String queryString =
            "SELECT COUNT(DISTINCT i)" +
                " FROM Instance i" +
                " LEFT JOIN i.experiments e" +
                " LEFT JOIN e.instrument instr" +
                " LEFT JOIN InstrumentScientist ir on ir.instrument = instr" +
                " LEFT JOIN ir.user user" +
                " LEFT JOIN i.members im" +
                " LEFT JOIN im.user owner on im.role = 'OWNER'" +
                " WHERE user = :user" +
                " AND i.deletedAt IS NULL";

        final TypedQuery<Long> query = this.createFilteredQuery(queryString, filter, null, null, Long.class);
        query.setParameter("user", user);

        return query.getSingleResult();
    }

    /**
     * Get all instances that are visible for an instrument scientist (instrument match)
     */
    public List<Instance> getAllForInstrumentScientist(User user, InstanceFilter filter, OrderBy orderBy, Pagination pagination) {
        String queryString =
            "SELECT DISTINCT i" +
                " FROM Instance i" +
                " LEFT JOIN i.experiments e" +
                " LEFT JOIN e.instrument instr" +
                " LEFT JOIN InstrumentScientist ir on ir.instrument = instr" +
                " LEFT JOIN ir.user user" +
                " LEFT JOIN i.members im" +
                " LEFT JOIN im.user owner on im.role = 'OWNER'" +
                " WHERE user = :user" +
                " AND i.deletedAt IS NULL";

        final TypedQuery<Instance> query = this.createFilteredQuery(queryString, filter, orderBy, pagination, Instance.class);
        query.setParameter("user", user);

        return query.getResultList();
    }


    /**
     * Get all active instances for instrument control support =>
     * - has anactive  experiment
     */
    public Long countAllForInstrumentControlSupport(int windowHalfWidthInDays, InstanceFilter filter) {
        String queryString =
            "SELECT COUNT(DISTINCT i)" +
                " FROM Instance i" +
                " LEFT JOIN i.experiments e" +
                " LEFT JOIN e.instrument instr" +
                " LEFT JOIN i.members im" +
                " LEFT JOIN im.user owner on im.role = 'OWNER'" +
                " WHERE e.startDate <= :periodEnd" +
                " AND e.endDate >= :periodStart" +
                " AND i.deletedAt IS NULL";

        final TypedQuery<Long> query = this.createFilteredQuery(queryString, filter, null, null, Long.class);
        Date periodStart = new Date(new Date().getTime() - windowHalfWidthInDays * 24 * 60 * 60 * 1000);
        Date periodEnd = new Date(new Date().getTime() + windowHalfWidthInDays * 24 * 60 * 60 * 1000);
        query.setParameter("periodStart", periodStart);
        query.setParameter("periodEnd", periodEnd);

        return query.getSingleResult();
    }

    /**
     * Get all active instances for instrument control support =>
     * - has an active experiment
     */
    public List<Instance> getAllForInstrumentControlSupport(int windowHalfWidthInDays, InstanceFilter filter, OrderBy orderBy, Pagination pagination) {
        String queryString =
            "SELECT DISTINCT i" +
                " FROM Instance i" +
                " LEFT JOIN i.experiments e" +
                " LEFT JOIN e.instrument instr" +
                " LEFT JOIN i.members im" +
                " LEFT JOIN im.user owner on im.role = 'OWNER'" +
                " WHERE e.startDate <= :periodEnd" +
                " AND e.endDate >= :periodStart" +
                " AND i.deletedAt IS NULL";

        final TypedQuery<Instance> query = this.createFilteredQuery(queryString, filter, orderBy, pagination, Instance.class);
        Date periodStart = new Date(new Date().getTime() - (long) windowHalfWidthInDays * 24 * 60 * 60 * 1000);
        Date periodEnd = new Date(new Date().getTime() + (long) windowHalfWidthInDays * 24 * 60 * 60 * 1000);
        query.setParameter("periodStart", periodStart);
        query.setParameter("periodEnd", periodEnd);

        return query.getResultList();
    }

    /**
     * Count all instances for IT support => all active instances
     */
    public Long countAllForITSupport(InstanceFilter filter) {
        String queryString =
            "SELECT COUNT(DISTINCT i) " +
                " FROM Instance i" +
                " LEFT OUTER JOIN i.experiments e" +
                " LEFT OUTER JOIN e.instrument instr" +
                " LEFT JOIN i.members im" +
                " LEFT JOIN im.user owner on im.role = 'OWNER'" +
                " WHERE i.deletedAt IS NULL";

        final TypedQuery<Long> query = this.createFilteredQuery(queryString, filter, null, null, Long.class);
        return query.getSingleResult();
    }

    public List<NumberInstancesByFlavour> countByFlavour() {
        final TypedQuery<NumberInstancesByFlavour> query = getEntityManager().createNamedQuery("instance.countByFlavour", NumberInstancesByFlavour.class);
        return query.getResultList();
    }

    public List<NumberInstancesByImage> countByImage() {
        final TypedQuery<NumberInstancesByImage> query = getEntityManager().createNamedQuery("instance.countByImage", NumberInstancesByImage.class);
        return query.getResultList();
    }

    /**
     * Get all instances for IT support => all active instances
     */
    public List<Instance> getAllForITSupport(InstanceFilter filter, OrderBy orderBy, Pagination pagination) {
        String queryString =
            "SELECT DISTINCT i " +
                " FROM Instance i" +
                " LEFT OUTER JOIN i.experiments e" +
                " LEFT OUTER JOIN e.instrument instr" +
                " LEFT JOIN i.members im" +
                " LEFT JOIN im.user owner on im.role = 'OWNER'" +
                " WHERE i.deletedAt IS NULL";

        final TypedQuery<Instance> query = this.createFilteredQuery(queryString, filter, orderBy, pagination, Instance.class);
        return query.getResultList();
    }

    /**
     * Get an instance that is visible for an instrument scientist (instrument match)
     */
    public Instance getByIdForInstrumentScientist(User user, Long instanceId) {
        try {
            final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getByIdForInstrumentScientist", Instance.class);
            query.setParameter("user", user);
            query.setParameter("instanceId", instanceId);

            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    /**
     * Get active instance that are accessible for all InstrumentControl support members
     */
    public Instance getByIdForInstrumentControlSupport(Long id, int windowHalfWidthInDays) {
        try {
            final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getByIdForExperimentBetweenDates", Instance.class);
            Date periodStart = new Date(new Date().getTime() - windowHalfWidthInDays * 24 * 60 * 60 * 1000);
            Date periodEnd = new Date(new Date().getTime() + windowHalfWidthInDays * 24 * 60 * 60 * 1000);
            query.setParameter("id", id);
            query.setParameter("periodStart", periodStart);
            query.setParameter("periodEnd", periodEnd);
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    /**
     * Get active instance that are accessible for all IT support members
     */
    public Instance getByIdForITSupport(Long id) {
        try {
            final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getById", Instance.class);
            query.setParameter("id", id);
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    public Instance getDeletedInstanceByComputeId(String computeId) {
        try {
            final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getDeletedByComputeId", Instance.class);
            query.setParameter("computeId", computeId);
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

   private <T> TypedQuery<T> createFilteredQuery(String queryString, InstanceFilter filter, OrderBy orderBy, Pagination pagination, Class<T> type) {
        List<Entry<String, Object>> queryParameters = new ArrayList<>();

        if (filter != null && filter.getId() != null) {
            queryString += " AND i.id = :id";
            queryParameters.add(new SimpleEntry<>("id", filter.getId()));
        }
        if (filter != null && filter.getName() != null) {
            queryString += " AND LOWER(i.name) like LOWER(:name)";
            queryParameters.add(new SimpleEntry<>("name", filter.getName() + "%"));
        }
        if (filter != null && filter.getInstrumentId() != null) {
            queryString += " AND instr.id = :instrId";
            queryParameters.add(new SimpleEntry<>("instrId", filter.getInstrumentId()));
        }
        if (filter != null && filter.getOwner() != null) {
            queryString += " AND owner.id = :owner";

            queryParameters.add(new SimpleEntry<>("owner", filter.getOwner()));
        }

        if (orderBy != null) {
            final String direction = orderBy.getAscending() ? "ASC" : "DESC";
            queryString += " ORDER BY i." + orderBy.getName() + " " + direction;
        }

        // Create query
        final TypedQuery<T> query = getEntityManager().createQuery(queryString, type);

        // Apply parameters
        for (Entry<String, Object> queryParameter : queryParameters) {
            query.setParameter(queryParameter.getKey(), queryParameter.getValue());
        }

        // Add pagination
        if (pagination != null) {
            final int offset = pagination.getOffset();
            final int limit = pagination.getLimit();
            query.setFirstResult(offset);
            query.setMaxResults(limit);
        }


        return query;
    }

    public Long countAllForUser(User user) {
        try {
            final TypedQuery<Long> query = getEntityManager().createNamedQuery("instance.countAllForUser", Long.class);
            query.setParameter("user", user);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public List<Instance> getAllForUserAndRole(User user, InstanceMemberRole role) {
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllForUserAndRole", Instance.class);
        query.setParameter("user", user)
            .setParameter("role", role);
        return query.getResultList();
    }

    public Long countAllForUserAndRole(User user, InstanceMemberRole role) {
        try {
            final TypedQuery<Long> query = getEntityManager().createNamedQuery("instance.countAllForUserAndRole", Long.class);
            query.setParameter("user", user)
                .setParameter("role", role);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }


    /**
     * Get thumbnail for the given instance
     */
    public InstanceThumbnail getThumbnailForInstance(Instance instance) {
        try {
            final TypedQuery<InstanceThumbnail> query = getEntityManager().createNamedQuery("instanceThumbnail.getForInstance", InstanceThumbnail.class);
            query.setParameter("instance", instance);
            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
    }

    public void saveThumbnail(InstanceThumbnail thumbnail) {
        final EntityManager entityManager = getEntityManager();
        if (thumbnail.getId() == null) {
            entityManager.persist(thumbnail);
        } else {
            entityManager.merge(thumbnail);
        }
    }

}
