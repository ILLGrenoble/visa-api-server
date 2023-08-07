package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceActivity;
import eu.ill.visa.core.domain.User;
import org.apache.commons.lang3.time.DateUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Singleton
public class InstanceActivityRepository extends AbstractRepository<InstanceActivity> {

    @Inject
    InstanceActivityRepository(final Provider<EntityManager> entityManager) {
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
