package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.entity.Experiment;

import jakarta.persistence.EntityManager;

public class ExperimentFilterProvider extends AbstractFilterQueryProvider<Experiment> {

    public ExperimentFilterProvider(EntityManager entityManager) {
        super(Experiment.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("instrument.id"),
            orderableField("instrument.name"),
            orderableField("proposal.id"),
            orderableField("proposal.identifier"),
            orderableField("users.id"),
            orderableField("startDate"),
            orderableField("endDate")
        );
    }

}

