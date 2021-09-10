package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.core.domain.Image;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.persistence.repositories.ImageRepository;

import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Transactional
@Singleton
public class ImageService {

    private ImageRepository repository;

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

    public List<Image> getAll(OrderBy orderBy, Pagination pagination) {
        return this.getAll(new QueryFilter(), orderBy, pagination);
    }

    public List<Image> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        return this.repository.getAll(filter, orderBy, pagination);
    }

    public List<Image> getAll(QueryFilter filter, Pagination pagination) {
        return this.repository.getAll(filter, null, pagination);
    }

    public List<Image> getAllForAdmin(Pagination pagination) {
        return this.repository.getAllForAdmin(pagination);
    }

    public List<Image> getAllForAdmin() {
        return this.repository.getAllForAdmin(null);
    }

    public Long countAll() {
        return repository.countAll(new QueryFilter());
    }

    public Long countAll(QueryFilter filter) {
        return repository.countAll(requireNonNullElseGet(filter, QueryFilter::new));
    }

    public Long countAllForAdmin() {
        return this.repository.countAllForAdmin();
    }
}
