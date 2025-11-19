package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Hypervisor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class HypervisorRepository extends AbstractRepository<Hypervisor> {

    @Inject
    HypervisorRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<Hypervisor> getAll() {
        final TypedQuery<Hypervisor> query = getEntityManager().createNamedQuery("hypervisor.getAll", Hypervisor.class);
        return query.getResultList();
    }

    public Hypervisor getById(Long id) {
        final TypedQuery<Hypervisor> query = getEntityManager().createNamedQuery("hypervisor.getById", Hypervisor.class);
        query.setParameter("id", id);
        try {
            return query.getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }

    public Long countAll() {
        final TypedQuery<Long> query = getEntityManager().createNamedQuery("hypervisor.countAll", Long.class);
        return query.getSingleResult();
    }

    public List<Hypervisor> getAllAvailable() {
        final TypedQuery<Hypervisor> query = getEntityManager().createNamedQuery("hypervisor.getAllAvailable", Hypervisor.class);
        return query.getResultList();
    }

    public void save(final Hypervisor hypervisor) {
        if (hypervisor.getId() == null) {
            persist(hypervisor);

        } else {
            merge(hypervisor);
        }
    }

    public void delete(final Hypervisor hypervisor) {
        this.remove(hypervisor);
    }

}
