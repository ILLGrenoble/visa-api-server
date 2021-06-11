package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class ConfigurationRepository extends AbstractRepository<Configuration> {

    @Inject
    public ConfigurationRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<Configuration> getAll() {
        final TypedQuery<Configuration> query = getEntityManager().createNamedQuery("configuration.getAll", Configuration.class);
        return query.getResultList();
    }
}
