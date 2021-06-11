package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.domain.Cycle;

import javax.persistence.EntityManager;

public class CycleFilterProvider extends AbstractFilterQueryProvider<Cycle> {

    public CycleFilterProvider(EntityManager entityManager) {
        super(Cycle.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("name"),
            orderableField("startDate"),
            orderableField("endDate")
        );
    }

}
