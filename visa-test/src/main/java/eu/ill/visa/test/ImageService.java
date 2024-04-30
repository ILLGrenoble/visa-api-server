package eu.ill.visa.test;

import eu.ill.visa.core.entity.Image;
import eu.ill.visa.persistence.repositories.ImageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ImageService {

    private final ImageRepository repository;
    private final TestImageRepository testImageRepository;

    @Inject
    public ImageService(final ImageRepository repository,
                        final TestImageRepository testImageRepository) {
        this.repository = repository;
        this.testImageRepository = testImageRepository;
    }

    public List<Image> getAll() {
        return this.repository.getAll();
    }
}
