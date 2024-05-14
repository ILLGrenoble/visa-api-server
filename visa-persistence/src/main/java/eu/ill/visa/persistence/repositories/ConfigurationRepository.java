package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Configuration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class ConfigurationRepository extends AbstractRepository<Configuration> {

    @Inject
    public ConfigurationRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<Configuration> getAll() {
        final TypedQuery<Configuration> query = getEntityManager().createNamedQuery("configuration.getAll", Configuration.class);
        return query.getResultList();
    }
}
