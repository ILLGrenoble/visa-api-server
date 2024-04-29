package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.entity.Plan;

import jakarta.persistence.EntityManager;

public class PlanFilterProvider extends AbstractFilterQueryProvider<Plan> {

    public PlanFilterProvider(EntityManager entityManager) {
        super(Plan.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("name"),
            orderableField("description"),
            orderableField("flavour.id"),
            orderableField("flavour.name"),
            orderableField("image.id"),
            orderableField("image.name")
        );
    }

}
