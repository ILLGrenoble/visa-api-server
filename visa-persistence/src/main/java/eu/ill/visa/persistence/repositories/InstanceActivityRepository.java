package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.InstanceActivity;
import eu.ill.visa.core.entity.User;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.List;

@Singleton
public class InstanceActivityRepository extends AbstractRepository<InstanceActivity> {

    @Inject
    InstanceActivityRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<InstanceActivity> getAll() {
        TypedQuery<InstanceActivity> query = getEntityManager().createNamedQuery("instanceActivity.getAll", InstanceActivity.class);
        return query.getResultList();
    }

    public List<InstanceActivity> getAllForUser(User user) {
        TypedQuery<InstanceActivity> query = getEntityManager().createNamedQuery("instanceActivity.getAllForUser", InstanceActivity.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public List<InstanceActivity> getAllForInstance(Instance instance) {
        TypedQuery<InstanceActivity> query = getEntityManager().createNamedQuery("instanceActivity.getAllForInstance", InstanceActivity.class);
        query.setParameter("instance", instance);
        return query.getResultList();
    }

    public void save(InstanceActivity instanceActivity) {
        if (instanceActivity.getId() == null) {
            persist(instanceActivity);

        } else {
            merge(instanceActivity);
        }
    }

    public void cleanup(int activityRetentionPeriod) {
        final Date date = DateUtils.addDays(new Date(), -activityRetentionPeriod);
        final Query query = getEntityManager().createNamedQuery("instanceActivity.cleanup");
        query.setParameter("date", date);

        query.executeUpdate();
    }
}
