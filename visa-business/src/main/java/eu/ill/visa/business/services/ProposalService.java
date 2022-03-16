package eu.ill.visa.business.services;

import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Proposal;
import eu.ill.visa.persistence.repositories.ProposalRepository;

import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;


@Transactional
@Singleton
public class ProposalService {

    private final ProposalRepository repository;

    @Inject
    public ProposalService(final ProposalRepository repository) {
        this.repository = repository;
    }

    public Proposal getById(@NotNull Long id) {
        return repository.getById(id);
    }

    public List<Proposal> getAll() {
        return repository.getAll();
    }
}
