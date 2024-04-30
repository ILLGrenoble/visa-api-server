package eu.ill.visa.persistence.repositories;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.preql.FilterQuery;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.Pagination;
import eu.ill.visa.core.domain.Parameter;
import eu.ill.visa.core.domain.QueryFilter;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNullElseGet;

abstract class AbstractRepository<T> {

    private EntityManager entityManager;

    // Required for Quarkus CDI (must have no-args constructor)
    // https://quarkus.io/guides/cdi-reference#simplified-constructor-injection
    protected AbstractRepository() {
    }

    protected AbstractRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void persist(final T object) {
        entityManager.persist(object);
        entityManager.flush();
    }

    public T merge(final T object) {
        T persistedObject = entityManager.merge(object);
        return persistedObject;
    }

    public void remove(final T object) {
        EntityManager em = entityManager;

        em.remove(em.contains(object) ? object : em.merge(object));
        em.flush();
    }

    public abstract List<T> getAll();

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public FilterQuery<T> createFilterQuery(AbstractFilterQueryProvider<T> provider, QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final FilterQuery<T> query = provider.createQuery(filter.getQuery());

        for (Parameter parameter : filter.getParameters()) {
            query.setParameter(parameter.getName(), parameter.getValue());
        }
        if (pagination != null) {
            query.setPagination(pagination.getLimit(), pagination.getOffset());
        }

        if (orderBy != null) {
            final String direction = orderBy.getAscending() ? "asc" : "desc";
            query.setOrder(orderBy.getName(), direction);
        }
        return query;
    }

    public Long countAll(AbstractFilterQueryProvider<T> provider, QueryFilter filter) {
        final FilterQuery<T> query = createFilterQuery(provider, requireNonNullElseGet(filter, QueryFilter::new), null, null);
        return query.count();
    }

    public List<T> getAll(AbstractFilterQueryProvider<T> provider, QueryFilter filter, OrderBy orderBy, Pagination pagination) {
        final FilterQuery<T> query = createFilterQuery(provider, requireNonNullElseGet(filter, QueryFilter::new), orderBy, pagination);
        return query.getResultList();
    }

    public List<T> getAll(AbstractFilterQueryProvider<T> provider, QueryFilter filter, OrderBy orderBy) {
        final FilterQuery<T> query = createFilterQuery(provider, requireNonNullElseGet(filter, QueryFilter::new), orderBy, null);
        return query.getResultList();
    }

    public void initialiseData(String filename) {
        final String sql = getFileAsString(filename);

        EntityManager em = this.getEntityManager();
        em.clear();
        Session session = em.unwrap(Session.class);
        session.doWork(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        });
    }

    private String getFileAsString(String fileName) {
        final InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if (is != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return null;
    }
}
