package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.ImageProtocol;
import eu.ill.visa.persistence.repositories.ImageProtocolRepository;
import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Transactional
@Singleton
public class ImageProtocolService {

    private final ImageProtocolRepository repository;

    @Inject
    public ImageProtocolService(ImageProtocolRepository repository) {
        this.repository = repository;
    }

    @Startup
    public void initImageProtocols() {
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
