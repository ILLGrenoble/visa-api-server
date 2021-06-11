package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.domain.FlavourLimit;

import javax.persistence.EntityManager;

public class FlavourLimitFilterProvider extends AbstractFilterQueryProvider<FlavourLimit> {

    public FlavourLimitFilterProvider(EntityManager entityManager) {
        super(FlavourLimit.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("objectId"),
            orderableField("objectType"),
            orderableField("flavour.id"),
            orderableField("flavour.name")
        );
    }

}
