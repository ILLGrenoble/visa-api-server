package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.InstanceActivity;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

@Singleton
public class InstanceActivityRepository extends AbstractRepository<InstanceActivity> {

    @Inject
    InstanceActivityRepository(final EntityManager entityManager) {
        super(entityManager);
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
