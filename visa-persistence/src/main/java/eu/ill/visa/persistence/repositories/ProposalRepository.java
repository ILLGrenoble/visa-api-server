package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.visa.core.domain.Proposal;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

@Singleton
public class ProposalRepository extends AbstractRepository<Proposal> {

    @Inject
    ProposalRepository(final Provider<EntityManager> entityManager) {
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
