package eu.ill.visa.persistence.repositories;

import eu.ill.visa.core.entity.Proposal;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Singleton
public class ProposalRepository extends AbstractRepository<Proposal> {

    @Inject
    ProposalRepository(final EntityManager entityManager) {
        super(entityManager);
    }

    public List<Proposal> getAll() {
        final TypedQuery<Proposal> query = getEntityManager().createNamedQuery("proposal.getAll", Proposal.class);
        return query.getResultList();
    }

    public Proposal getById(final Long id) {
        try {
            final TypedQuery<Proposal> query = getEntityManager().createNamedQuery("proposal.getById", Proposal.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

}
