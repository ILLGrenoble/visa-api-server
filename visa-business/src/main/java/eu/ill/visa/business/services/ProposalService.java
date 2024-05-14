package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Proposal;
import eu.ill.visa.persistence.repositories.ProposalRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

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
