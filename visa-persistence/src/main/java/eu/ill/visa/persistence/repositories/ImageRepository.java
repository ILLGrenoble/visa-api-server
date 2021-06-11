package eu.ill.visa.persistence.repositories;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import eu.ill.preql.FilterQuery;
import eu.ill.visa.core.domain.Image;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.persistence.providers.ImageFilterProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Singleton
public class ImageRepository extends AbstractRepository<Image> {


    @Inject
    ImageRepository(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<Image> getAll() {
        final TypedQuery<Image> query = getEntityManager().createNamedQuery("image.getAll", Image.class);
        return query.getResultList();
    }

    public List<Image> getAll(QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final ImageFilterProvider provider = new ImageFilterProvider(getEntityManager());
        final FilterQuery<Image> query = createFilterQuery(provider, requireNonNullElseGet(filter, QueryFilter::new), orderBy, pagination);
        query.addExpression((criteriaBuilder, root) ->
            criteriaBuilder.equal(root.get("deleted"), false)
        );
        return query.getResultList();
    }

    public List<Image> getAllForAdmin(Pagination pagination) {
        final TypedQuery<Image> query = getEntityManager().createNamedQuery("image.getAllForAdmin", Image.class);
        if (pagination != null) {
            final int offset = pagination.getOffset();
            final int limit = pagination.getLimit();
            query.setFirstResult(offset);
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }



    public Long countAll(QueryFilter filter) {
        final ImageFilterProvider provider = new ImageFilterProvider(getEntityManager());
        final FilterQuery<Image> query = createFilterQuery(provider, requireNonNullElseGet(filter, QueryFilter::new), null, null);
        query.addExpression((criteriaBuilder, root) ->
            criteriaBuilder.equal(root.get("deleted"), false)
        );
        return query.count();
    }

    public Long countAllForAdmin() {
        final TypedQuery<Long> query = getEntityManager().createNamedQuery("image.countAllForAdmin", Long.class);
        return query.getSingleResult();
    }

    public Image getById(final Long id) {
        try {
            final TypedQuery<Image> query = getEntityManager().createNamedQuery("image.getById", Image.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    public void delete(final Image image) {
        remove(image);
    }

    public void save(final Image image) {
        if (image.getId() == null) {
            persist(image);

        } else {
            merge(image);
        }
    }
}
