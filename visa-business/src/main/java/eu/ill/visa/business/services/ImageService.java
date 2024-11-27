package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.Image;
import eu.ill.visa.persistence.repositories.ImageRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Transactional
@Singleton
public class ImageService {

    private final ImageRepository repository;

    @Inject
    public ImageService(ImageRepository repository) {
        this.repository = repository;
    }

    public List<Image> getAll() {
        return this.repository.getAll();
    }

    public Image getById(Long id) {
        return this.repository.getById(id);
    }

    public void delete(Image image) {
        this.repository.delete(image);
    }

    public void save(@NotNull Image image) {
        this.repository.save(image);
    }

    public List<Image> getAllForAdmin() {
        return this.repository.getAllForAdmin();
    }

    public Long countAllForAdmin() {
        return this.repository.countAllForAdmin();
    }
}
