package eu.ill.visa.business.services;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import eu.ill.visa.core.entity.ImageProtocol;
import eu.ill.visa.persistence.repositories.ImageProtocolRepository;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Transactional
@ApplicationScoped
public class ImageProtocolService {

    private final ImageProtocolRepository repository;

    @Inject
    public ImageProtocolService(ImageProtocolRepository repository) {
        this.repository = repository;

        // Initialise data if empty
        if (this.getAll().isEmpty()) {
            this.repository.initialise();
        }
    }

    public List<ImageProtocol> getAll() {
        return this.repository.getAll();
    }

    public ImageProtocol getById(Long id) {
        return this.repository.getById(id);
    }

    public void delete(ImageProtocol imageProtocol) {
        this.repository.delete(imageProtocol);
    }

    public void create(@NotNull ImageProtocol imageProtocol) {
        this.repository.save(imageProtocol);
    }

    public ImageProtocol getByName(@NotNull String name) {
        return this.repository.getByName(name);
    }
}
