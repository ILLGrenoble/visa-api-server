package eu.ill.visa.business.services;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.ImageProtocol;
import eu.ill.visa.persistence.repositories.ImageProtocolRepository;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Transactional
@Singleton
public class ImageProtocolService {

    private ImageProtocolRepository repository;

    @Inject
    public ImageProtocolService(ImageProtocolRepository repository) {
        this.repository = repository;

        // Initialise data if empty
        if (this.getAll().size() == 0) {
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
