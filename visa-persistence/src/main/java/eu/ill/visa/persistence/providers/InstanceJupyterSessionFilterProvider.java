package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.domain.InstanceJupyterSession;

import javax.persistence.EntityManager;

public class InstanceJupyterSessionFilterProvider extends AbstractFilterQueryProvider<InstanceJupyterSession> {

    public InstanceJupyterSessionFilterProvider(EntityManager entityManager) {
        super(InstanceJupyterSession.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("createdAt"),
            field("active")
        );
    }

}
