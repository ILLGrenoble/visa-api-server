package eu.ill.visa.test;

import eu.ill.visa.core.entity.Image;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

@ApplicationScoped
public class TestImageRepository {

    private final EntityManager entityManager;

    public TestImageRepository(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Image> getAll() {
        String queryString = """
            SELECT i
            FROM Image i
            ORDER BY i.id
        """;
        TypedQuery<Image> query = entityManager.createQuery(queryString, Image.class);

        return query.getResultList();
    }
}
