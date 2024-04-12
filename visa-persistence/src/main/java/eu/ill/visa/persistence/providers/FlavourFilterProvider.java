package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.domain.Flavour;

import jakarta.persistence.EntityManager;

public class FlavourFilterProvider extends AbstractFilterQueryProvider<Flavour> {

    public FlavourFilterProvider(EntityManager entityManager) {
        super(Flavour.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("name"),
            orderableField("memory"),
            orderableField("cpu"),
            orderableField("computeId")
        );
    }

}
