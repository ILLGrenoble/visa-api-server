package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.domain.filters.InstanceFilter;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import eu.ill.visa.core.entity.partial.InstancePartial;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

@Singleton
public class InstanceRepository extends AbstractRepository<Instance> {

    private static final Logger logger = LoggerFactory.getLogger(InstanceRepository.class);

    @Inject
    InstanceRepository(final EntityManager entityManager) {
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

    public List<Instance> getAllWithMembersForInstances(List<Instance> instances) {
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllWithMembersForInstances", Instance.class);
        query.setParameter("instances", instances);

        return query.getResultList();
    }

    public List<Instance> getAllWithExperimentsForInstances(List<Instance> instances) {
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllWithExperimentsForInstances", Instance.class);
        query.setParameter("instances", instances);

        return query.getResultList();
    }

    public List<Instance> getAllWithAttributesForInstances(List<Instance> instances) {
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllWithAttributesForInstances", Instance.class);
        query.setParameter("instances", instances);

        return query.getResultList();
    }

    public List<Instance> getAllWithStates(List<InstanceState> states) {
        final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getAllWithStates", Instance.class);
        query.setParameter("states", states);

        return query.getResultList();
    }

    public Long getIdByUid(final String uid) {
        try {
            final TypedQuery<Long> query = getEntityManager().createNamedQuery("instance.getIdByUid", Long.class);
            query.setParameter("uid", uid);
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

    public List<Instance> getAll(final InstanceFilter filter, final OrderBy orderBy, final Pagination pagination) {
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Instance> cbQuery = cb.createQuery(Instance.class);
        final Root<Instance> root = cbQuery.from(Instance.class);

        Fetch<Instance, Plan> planFetch = root.fetch("plan", JoinType.INNER);
        Fetch<Plan, Image> imageFetch = planFetch.fetch("image", JoinType.INNER);
        imageFetch.fetch("protocols", JoinType.LEFT);
        planFetch.fetch("flavour", JoinType.INNER);

        InstanceRequestContext context = new InstanceRequestContext(root);
        final List<Predicate> predicates = this.convertFilterToPredicates(filter, cb, context);
        cbQuery.where(cb.and(predicates.toArray(new Predicate[0]))).distinct(true);

        if (orderBy != null) {
            if (orderBy.getName().equals("id")) {
                Path<Long> idOrder = root.get("id");
                cbQuery.orderBy(orderBy.getAscending() ? cb.asc(idOrder) : cb.desc(idOrder));

            } else if (orderBy.getName().equals("createdAt")) {
                Path<Date> createdAtOrder = root.get("createdAt");
                cbQuery.orderBy(orderBy.getAscending() ? cb.asc(createdAtOrder) : cb.desc(createdAtOrder));

            }  else if (orderBy.getName().equals("name")) {
                Path<String> nameOrder = root.get("name");
                cbQuery.orderBy(orderBy.getAscending() ? cb.asc(nameOrder) : cb.desc(nameOrder));

            }  else if (orderBy.getName().equals("state")) {
                Path<String> stateOrder = root.get("state");
                cbQuery.orderBy(orderBy.getAscending() ? cb.asc(stateOrder) : cb.desc(stateOrder));

            } else if (orderBy.getName().equals("terminationDate")) {
                Path<Date> terminationOrder = root.get("terminationDate");
                cbQuery.orderBy(orderBy.getAscending() ? cb.asc(terminationOrder) : cb.desc(terminationOrder));

            } else {
                logger.warn("Client has requested ordering of instances by unknown field: {}", orderBy.getName());
            }
        }

        TypedQuery<Instance> query = getEntityManager().createQuery(cbQuery);


        if (pagination != null) {
            query.setFirstResult(pagination.getOffset());
            query.setMaxResults(pagination.getLimit());
        }

        return query.getResultList();
    }

    public Long countAll(final InstanceFilter filter) {
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Long> cbQuery = cb.createQuery(Long.class);
        final Root<Instance> root = cbQuery.from(Instance.class);

        InstanceRequestContext context = new InstanceRequestContext(root);
        final List<Predicate> predicates = this.convertFilterToPredicates(filter, cb, context);

        cbQuery.where(cb.and(predicates.toArray(new Predicate[0])));

        cbQuery.select(cb.countDistinct(root));
        TypedQuery<Long> query = getEntityManager().createQuery(cbQuery);
        return query.getSingleResult();
    }


    protected List<Predicate> convertFilterToPredicates(final InstanceFilter filter,
                                                        final CriteriaBuilder cb,
                                                        final InstanceRequestContext context) {

        final Root<Instance> root = context.getRoot();

        final List<Predicate> predicates = new ArrayList<>();

        if (filter.getIds() != null && !filter.getIds().isEmpty()) {
            predicates.add(root.get("id").in(filter.getIds()));
        }

        if (filter.getNameLike() != null) {
            String nameLike = String.format("%%%s%%", filter.getNameLike()).toLowerCase();
            predicates.add(cb.like(cb.lower(root.get("name")), nameLike));
        }

        if (filter.getOwnerId() != null) {
            Join<Instance, InstanceMember> memberJoin = context.getMemberJoin();
            predicates.add(cb.equal(memberJoin.get("user").get("id"), filter.getOwnerId()));
            predicates.add(cb.equal(memberJoin.get("role"), InstanceMemberRole.OWNER));
        }

        if (filter.getInstrumentId() != null) {
            Join<Instance, Experiment> experimentJoin = context.getExperimentJoin();
            predicates.add(cb.equal(experimentJoin.get("instrument").get("id"), filter.getInstrumentId()));
        }

        if (filter.getInstrumentId() != null) {
            Join<Instance, Experiment> experimentJoin = context.getExperimentJoin();
            predicates.add(cb.equal(experimentJoin.get("instrument").get("id"), filter.getInstrumentId()));
        }

        if (filter.getImageId() != null) {
            Join<Instance, Plan> planJoin = context.getPlanJoin();
            predicates.add(cb.equal(planJoin.get("image").get("id"), filter.getImageId()));
        }

        if (filter.getFlavourId() != null) {
            Join<Instance, Plan> planJoin = context.getPlanJoin();
            predicates.add(cb.equal(planJoin.get("flavour").get("id"), filter.getFlavourId()));
        }

        predicates.add(cb.isNull(root.get("deletedAt")));

        return predicates;
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

    public List<NumberInstancesByCloudClient> countByCloudClient() {
        final TypedQuery<NumberInstancesByCloudClient> query = getEntityManager().createNamedQuery("instance.countByCloudClient", NumberInstancesByCloudClient.class);
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
     * Get an instance for an owner
     */
    public Instance getByIdForOwner(User user, String instanceUid) {
        try {
            final TypedQuery<Instance> query = getEntityManager().createNamedQuery("instance.getByUidForOwner", Instance.class);
            query.setParameter("user", user);
            query.setParameter("instanceUid", instanceUid);

            return query.getSingleResult();

        } catch (NoResultException exception) {
            return null;
        }
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

        if (filter != null && filter.getIds() != null && !filter.getIds().isEmpty()) {
            queryString += " AND i.id IN (:ids)";
            queryParameters.add(new SimpleEntry<>("ids", filter.getIds()));
        }
        if (filter != null && filter.getNameLike() != null) {
            queryString += " AND LOWER(i.name) like LOWER(:name)";
            queryParameters.add(new SimpleEntry<>("name", filter.getNameLike() + "%"));
        }
        if (filter != null && filter.getInstrumentId() != null) {
            queryString += " AND instr.id = :instrId";
            queryParameters.add(new SimpleEntry<>("instrId", filter.getInstrumentId()));
        }
        if (filter != null && filter.getOwnerId() != null) {
            queryString += " AND owner.id = :owner";

            queryParameters.add(new SimpleEntry<>("owner", filter.getOwnerId()));
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
    public InstanceThumbnail getThumbnailForInstanceUid(String instanceUid) {
        try {
            final TypedQuery<InstanceThumbnail> query = getEntityManager().createNamedQuery("instanceThumbnail.getForInstanceUid", InstanceThumbnail.class);
            query.setParameter("instanceUid", instanceUid);
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

    public InstancePartial getPartialById(Long id) {
        try {
            final TypedQuery<InstancePartial> query = getEntityManager().createNamedQuery("instance.getPartialById", InstancePartial.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void updatePartialById(InstancePartial instance) {
        getEntityManager().createNamedQuery("instance.updatePartialById")
            .setParameter("id", instance.getId())
            .setParameter("lastSeenAt", instance.getLastSeenAt())
            .setParameter("lastInteractionAt", instance.getLastInteractionAt())
            .executeUpdate();
    }


    protected static final class InstanceRequestContext {
        private final Root<Instance> root;
        private Join<Instance, InstanceMember> memberJoin = null;
        private Join<Instance, Experiment> experimentJoin = null;
        private Join<Instance, Plan> planJoin = null;

        public InstanceRequestContext(Root<Instance> root) {
            this.root = root;
        }

        public Root<Instance> getRoot() {
            return root;
        }

        public Join<Instance, InstanceMember> getMemberJoin() {
            if (memberJoin == null) {
                memberJoin = root.join("members", JoinType.INNER);
            }
            return memberJoin;
        }

        public Join<Instance, Experiment> getExperimentJoin() {
            if (experimentJoin == null) {
                experimentJoin = root.join("experiments", JoinType.LEFT);
            }
            return experimentJoin;
        }

        public Join<Instance, Plan> getPlanJoin() {
            if (planJoin == null) {
                planJoin = root.join("plan", JoinType.INNER);
            }
            return planJoin;
        }
    }
}
